package com.emiliano.androidTestTool.core;

import java.util.List;
import java.util.Map;

import com.emiliano.androidTestTool.core.components.Component;
import com.emiliano.androidTestTool.core.metrics.Metric;
import com.emiliano.androidTestTool.core.metrics.OperationMetric;

//import android.app.ProgressDialog;
//import android.content.Context;
import android.os.AsyncTask;

public class Executor<Input, Output> extends AsyncTask<TestPlan<Input, Output>, String, Results<Input, Output>[]> {

	private ExecutorListener listener;

	public Executor(ExecutorListener listener) {
		this.listener = listener;
	}

	public Executor() {
		this(null);
	}

	@Override
	protected Results<Input, Output>[] doInBackground(TestPlan<Input, Output>... params) {
		Results<Input, Output>[] results = new Results[params.length];
		for (int tp = 0; tp < params.length; tp++) {
			TestPlan<Input, Output> testPlan = params[tp];
			if (testPlan != null) {
				results[tp] = executeTestPlan(testPlan);
			}
		}
		return results;
	}

	protected Results<Input, Output> executeTestPlan(TestPlan<Input, Output> testPlan) {
		this.publishProgress("executing " + testPlan.getName());
		ResultsImpl<Input, Output> results = new ResultsImpl<Input, Output>(testPlan);

		for (Metric<TestPlan<Input, Output>, ?> globalMetric : testPlan.getGlobalMetrics()) {
			results.addGlobalMeasure(globalMetric.getName(), globalMetric.calculate(testPlan));
		}

		List<Loader<Input>> inputs = testPlan.getInputs();
		List<Component<Input, Output>> components = testPlan.getComponents();
		
		for (int c = 0; c < components.size(); c++) {
			for (Metric<Component<Input, Output>, ?> componentMetric : testPlan.getComponentMetrics()) {
				results.addComponentMeasure(c, componentMetric.getName(), componentMetric.calculate(components.get(c)));
			}
		}

		int currentOperation = 0;
		int totalOperations = inputs.size() * components.size();

		for (int i = 0; i < inputs.size(); i++) {
			Loader<Input> inputLoader= inputs.get(i);
			inputLoader.loadElement();
			Input input = inputLoader.getElement();

			for (Metric<Input, ?> inputMetric : testPlan.getInputMetrics()) {
				results.addInputMeasure(i, inputMetric.getName(), inputMetric.calculate(input));
			}
			
			for (int c = 0; c < components.size(); c++) {
				Component<Input, Output> component = components.get(c);

				currentOperation++;
				if(currentOperation%100==0)
					this.publishProgress("executing " + testPlan.getName() + ": operation " + currentOperation + " of "
						+ totalOperations);

				for (OperationMetric<Input, Output, ?> operationMetric : testPlan.getOperationMetrics()) {
					operationMetric.onBeforeOperation(input, component);
				}
				System.gc();
				Output output = component.execute(input);
				for (OperationMetric<Input, Output, ?> operationMetric : testPlan.getOperationMetrics()) {
					results.addOperationMeasure(i, c, operationMetric.getName(), operationMetric.calculate(output));
				}

				if (testPlan.getDelayBetweenOperationsMillis() > 0) {
					synchronized (this) {
						try {
							wait(testPlan.getDelayBetweenOperationsMillis());
						} catch (InterruptedException exeption) {
							exeption.printStackTrace();
						}
					}
				}

			}
			
			inputLoader.releaseElement();
		}
		return results;
	}

	@Override
	protected void onPreExecute() {
		if (listener != null)
			this.listener.onPreExecute();
	}

	@Override
	protected void onProgressUpdate(String... progress) {
		if (listener != null)
			this.listener.onProgressUpdate(progress);
	}

	@Override
	protected void onPostExecute(Results<Input, Output>[] result) {
		if (listener != null)
			this.listener.onPostExecute(result);
	}

	public interface ExecutorListener {
		public void onPreExecute();

		public void onProgressUpdate(String... progress);

		public void onPostExecute(Results[] results);

	}

}
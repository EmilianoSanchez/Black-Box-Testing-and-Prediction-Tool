package com.emiliano.examplesatt.examples.matrix;

import com.emiliano.androidTestTool.core.TestPlan;
import com.emiliano.androidTestTool.utils.TestToolActivity;

public class ActivityMatrix extends TestToolActivity<MatrixPair,double[][]>{

	@Override
	protected TestPlan<MatrixPair,double[][]> getPlan() {
		return new TestPlanMatrix(this);
	}
	
	@Override
	protected String getFileName() {
		return "Matrix multiplication";
	}
}
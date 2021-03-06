package com.emiliano.androidMeter.core;

import java.util.List;
import java.util.Map;

public interface Results<Input, Output> {

	public BenchmarkPlan<Input, Output> getBenchmarkPlan();

	public Map<String, Object> getGlobalMeasures();

	public List<Map<String, Object>> getInputMeasures();

	public Map<String, Object> getInputMeasures(int inputIndex);

	public List<Map<String, Object>> getComponentMeasures();

	public Map<String, Object> getComponentMeasures(int componentIndex);

	public List<List<Map<String, Object>>> getOperationMeasures();

	public Map<String, Object> getOperationMeasures(int inputIndex, int componentIndex);

}

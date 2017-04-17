package ParallelExecution;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.workflowsim.CondorVM;
import org.workflowsim.Job;
import org.workflowsim.WorkflowDatacenter;
import org.workflowsim.WorkflowEngine;
import org.workflowsim.WorkflowPlanner;
import org.workflowsim.examples.scheduling.DataAwareSchedulingAlgorithmExample;
import org.workflowsim.utils.ClusteringParameters;
import org.workflowsim.utils.OverheadParameters;
import org.workflowsim.utils.Parameters;
import org.workflowsim.utils.ReplicaCatalog;

public class FCFSParallel extends DataAwareSchedulingAlgorithmExample{
	
	public static void main(String[] args){
		
		try {
			// First step: Initialize the WorkflowSim package.
			/**
			 * However, the exact number of vms may not necessarily be vmNum If
			 * the data center or the host doesn't have sufficient resources the
			 * exact vmNum would be smaller than that. Take care.
			 */
			int vmNum = 5;
			System.out.println(args[0]); 
			File daxFile = new File(args[0]);
			if (!daxFile.exists()) {
				Log.printLine("Warning: Please replace daxPath with the physical path in your working environment!");
				return;
			}
		
			//number of threads =vms 
			//call the fcfsschedulingalgorithm from the workflowsim.scheduling
		
			/**
			 * Since we are using HEFT planning algorithm, the scheduling
			 * algorithm should be static such that the scheduler would not
			 * override the result of the planner
			 */
			Parameters.SchedulingAlgorithm sch_method = Parameters.SchedulingAlgorithm.FCFS;
			Parameters.PlanningAlgorithm pln_method = Parameters.PlanningAlgorithm.INVALID;
			ReplicaCatalog.FileSystem file_system = ReplicaCatalog.FileSystem.LOCAL;

			/**
			 * No overheads
			 */
			OverheadParameters op = new OverheadParameters(0, null, null, null,null, 0);

			/**
			 * No Clustering
			 */
			ClusteringParameters.ClusteringMethod method = ClusteringParameters.ClusteringMethod.NONE;
			ClusteringParameters cp = new ClusteringParameters(0, 0, method,
					null);

			/**
			 * Initialize static parameters
			 */
			Parameters.init(vmNum, args[0], null, null, op, cp, sch_method,
					pln_method, null, 0);
			ReplicaCatalog.init(file_system);

			// before creating any entities.
			int num_user = 1; // number of grid users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			WorkflowDatacenter datacenter0 = createDatacenter("Datacenter_0");

			/**
			 * Create a WorkflowPlanner with one schedulers.
			 */
			WorkflowPlanner wfPlanner = new WorkflowPlanner("planner_0", 1);
			/**
			 * Create a WorkflowEngine.This releases tasks that are free for execution to workflow scheduler
			 */
			WorkflowEngine wfEngine = wfPlanner.getWorkflowEngine();
			/**
			 * Create a list of VMs.The userId of a vm is basically the id of
			 * the scheduler that controls this vm. 
			 */			
			//create vms using threads here. 
			MyThread t1 = new MyThread(wfEngine.getSchedulerId(0),1);
			MyThread t2 = new MyThread(wfEngine.getSchedulerId(0),1);
			MyThread t3 = new MyThread(wfEngine.getSchedulerId(0),1);
			MyThread t4 = new MyThread(wfEngine.getSchedulerId(0),1);
			MyThread t5 = new MyThread(wfEngine.getSchedulerId(0),1);
			
		//	List<CondorVM> vmlist0 = createVM(wfEngine.getSchedulerId(0), Parameters.getVmNum()); //pass the parameters as the parameteres to the thread. And cll createvm in run. 
			t1.start();
			t2.start();
			t3.start();
			t4.start();
			t5.start();
			
			//after all the vms are created, add them to vmlist0

			/**
			 * Submits this list of vms to this WorkflowEngine.
			 */
			//wfEngine.submitVmList(vmlist0, 0);

			/**
			 * Binds the data centers with the scheduler.
			 */
			wfEngine.bindSchedulerDatacenter(datacenter0.getId(), 0);

			CloudSim.startSimulation();
			List<Job> outputList0 = wfEngine.getJobsReceivedList();
			CloudSim.stopSimulation();
			printJobList(outputList0);
		} catch (Exception e) {
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
		
		
	}
}

class MyThread extends Thread{

	/*private int startIdx, nThreads, maxIdx;
	//Each thread will have the variable as vm....
	public MyThread(int s, int n, int m)
	{
		this.startIdx = s;
		this.nThreads = n;
		this.maxIdx = m;		
	}
	
	@Override
	public void run()
	{ 
		for(int i = this.startIdx; i<this.maxIdx;i+=this.nThreads) //limitations will be the number of tasks
		{
			System.out.println("[ID " + this.getId() + "] " + i );  //business logic to go here. 
			//each thread will only call maxchildscheduling algorithm 
			System.out.println("[Thread" + this.getId() + "] allocates task 'here id of task needs to be written' to 'enter the VM id here' ");
		}
	}*/
	
	int schedulerId,VmNum;
	
	public MyThread (int schedId, int Vm){
		
		this.schedulerId = schedId;
		this.VmNum = Vm;
		
	}
	
	public void run(){
		
		Thread t = Thread.currentThread();
		long threadLong = t.getId();
		int threadId = (int) threadLong;
		createVM(this.schedulerId, threadId); // copy the code for createvm here if we cannot access the calling method. 
	}
	
	void createVM(int userId, int threadId){
		
		long size = 10000; // image size (MB)
		int ram = 512; // vm memory (MB)
		int mips = 1000;
		long bw = 1000;
		int pesNumber = 1; // number of cpus
		String vmm = "Xen"; // VMM name
		
		Random bwRandom = new Random(System.currentTimeMillis());
		double ratio = bwRandom.nextDouble();
			CondorVM vm = new CondorVM(threadId, userId, mips * ratio, pesNumber, ram,
					(long) (bw * ratio), size, vmm, new CloudletSchedulerSpaceShared());
			System.out.println("VM created with thread" + threadId);
	}
}
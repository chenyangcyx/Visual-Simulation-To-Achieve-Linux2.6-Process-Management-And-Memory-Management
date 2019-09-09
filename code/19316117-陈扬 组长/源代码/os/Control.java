package os;

import hardware.CPU;

public class Control
{
	public static Control con=new Control();
	
	public void SystemStart()
	{
		CPU.cpu.ti.start();			//启动计时器线程
		Scheduling.sch.start();		//启动三级调度线程
		//启动死锁检测线程
	}
	
	public boolean IfAllTaskOK()
	{
		//检测是否所有作业、进程已经运行完毕
		boolean if_ok=true;
		//检测运行队列是否为空
		if(!ProcessModule.process_module.IsRunningQueueEmpty())
			if_ok=false;
		//检测就绪队列是否为空
		if(!ProcessModule.process_module.IsReadyQueueEmpty())
			if_ok=false;
		//检测等待队列是否为空
		if(!ProcessModule.process_module.IsWaitQueueEmpty())
			if_ok=false;
		//检测挂起队列是否为空
		if(!ProcessModule.process_module.IsSuspendQueueEmpty())
			if_ok=false;
		//检测后备队列是否为空
		if(!JobModule.job_module.IsJobListEmpty())
			if_ok=false;
		return if_ok;
	}
}

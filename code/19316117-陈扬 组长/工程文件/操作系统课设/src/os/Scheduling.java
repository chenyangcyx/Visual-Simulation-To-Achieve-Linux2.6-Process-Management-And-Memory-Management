package os;

import java.util.ArrayList;

import hardware.CPU;
import ui.CPUInfoUI;
import ui.HardDiskUI;
import ui.MainUI;
import ui.MemoryUI;
import ui.PageModuleUI;

public class Scheduling extends Thread
{
	public static Scheduling sch=new Scheduling();
	
	public boolean if_wait=false;		//当前是否正处于等待态
	public int wait_time=-1;			//等待态还剩余时间
	
	public void run()
	{
		while(true)
		{
			try
			{
				sleep(1);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			if(CPU.cpu.ti.GetIfInterrupt()==true)
			{
				if(kernel.SYSTEM_TIME%5000==0)
					HighLevelScheduling();		//高级调度
				//死锁检测
				if(kernel.SYSTEM_TIME%3500==0)
				{
					MainUI.main_ui.run_info+="\n※※死锁检测--";
					ArrayList<PCB> dl=DeadLock.dl.CheckDeadLock();
					if(dl.size()==0)
					{
						MainUI.main_ui.run_info+="未检测到死锁！\n";
					}
					else
					{
						MainUI.main_ui.run_info+="检测到死锁！与死锁有关进程：\n";
						for(int i=0;i<dl.size();i++)
						{
							MainUI.main_ui.run_info+=dl.get(i)+"，Process_ID："+dl.get(i).GetPid()+"\n";
						}
						MainUI.main_ui.run_info+="\n调用进程撤销原语，撤销进程！\n";
						for(int i=0;i<dl.size();i++)
						{
							dl.get(i).ProcessCancel(); 	//进程撤销原语，撤销进程
						}
					}
				}
				if(kernel.SYSTEM_TIME%2000==0)
				{
					PageModuleUI.page_ui.RefreshData(); 	//页面UI的刷新
					HardDiskUI.hd_ui.RefreshData();			//磁盘UI的刷新
					MemoryUI.mem_ui.RefreshData();			//内存UI的刷新
					MiddleLevelScheduling();	//中级调度
				}
				if(kernel.SYSTEM_TIME%10==0)
					LowLevelScheduling();		//低级调度
				UIRefresh();
				CPU.cpu.ti.ResetIfInterrupt(); 	//重置计时器中断
				//检测是否运行玩成
				if(Control.con.IfAllTaskOK()==true)
				{
					MainUI.main_ui.run_info+="\n\n进程全部运行完毕，程序停止！\n\n";
					UIRefresh();
					break;
				}
			}
		}
	}
	
	public void UIRefresh()
	{
		//不同UI的刷新
		MainUI.main_ui.RefreshData(); 		//主界面UI的刷新
		CPUInfoUI.cpu_info_ui.RefreshData(); 	//CPU界面UI的刷新
	}
	
	public void SuspendProcessWithPageNum(short num)
	{
		//检测某一页所关联的进程，并将该进程加入到挂起态
		for(int i=0;i<ProcessModule.process_module.all_queue.size();i++)
		{
			PCB t=ProcessModule.process_module.all_queue.get(i);
			for(int j=0;j<(kernel.MEMORY_USER_SPACE_SIZE)/kernel.SINGLE_PAGE_SIZE;j++)
			{
				if(t.page_table[i][1]==num)
				{
					int page_num=PageModule.page_module.GetOneFreePageInDisk();
					ProcessModule.process_module.ChangePageTable(num,(short) page_num);
					t.ProcessSuspend();
				}
			}
		}
	}
	
	public void HighLevelScheduling()
	{
		//高级调度
		//检测后备队列是否有新的未被导入的作业
		MainUI.main_ui.run_info+="系统时间："+kernel.SYSTEM_TIME+"高级调度--";
		JobModule.job_module.RefreshJobList();			//刷新后备队列
		MainUI.main_ui.run_info+="检测后备队列是否有作业--";
		if(!JobModule.job_module.IsAllJobToProcess())	//后备队列中还有作业
		{
			while(!JobModule.job_module.job_list.isEmpty())
			{
				MainUI.main_ui.run_info+="后备队列有作业--";
				JCB jcb=JobModule.job_module.job_list.get(0);	//获取最近的一个作业
				MainUI.main_ui.run_info+="作业序号："+jcb.GetJobid()+"等待被调入--";			
				//检查PCB池是否空间足够
				MainUI.main_ui.run_info+="检测PCB池是否足够--";
				if(ProcessModule.process_module.GetFreePCBNumInPool()>=1)
				{
					MainUI.main_ui.run_info+="PCB空间足够--";
					//PCB池空间足够
					//检测虚存空间是否足够
					MainUI.main_ui.run_info+="检测虚存空间是否足够--";
					if(jcb.GetPagesNum()<=PageModule.page_module.GetFreePageNumInDisk()-1)
					{
						//虚存空间足够
						MainUI.main_ui.run_info+="虚存空间足够--";
						//第一步，JCB转换为PCB
						MainUI.main_ui.run_info+="将JCB转换为PCB--";
						PCB pro=ProcessModule.process_module.TurnJCBToPCB(jcb);
						//第二步，在虚存中申请对应大小的空间
						MainUI.main_ui.run_info+="在虚存中申请空间--";
						String swap_area_apply=PageModule.page_module.ApplyPageInDisk(pro.GetPagesNum());
						//第三步，将作业的所有指令区调入到虚存中
						MainUI.main_ui.run_info+="作业载入--";
						ProcessModule.process_module.TransferJobCodeToSwapArea(jcb,swap_area_apply);
						//第四步，写PCB中的页表
						MainUI.main_ui.run_info+="写入页表--";
						ProcessModule.process_module.WriteProcessPageTable(pro, swap_area_apply);
						//第五步，将PCB写入到PCB池
						MainUI.main_ui.run_info+="写PCB池--";
						ProcessModule.process_module.AddToPCBPool(pro);
						//第六步，指针后移
						JobModule.job_module.NextJob();
						JobModule.job_module.RefreshJobList();			//刷新后备队列
						//第七步，将该进程加入到就绪队列，等待被调度
						MainUI.main_ui.run_info+="进程加入到就绪队列，等待被调度\n";
						ProcessModule.process_module.TransferProcessToReadyQueue(pro, true);
						//pro.ProcessCreate(); 	//调用进程创建原语
					}
					else //虚存空间不足没有空间
						MainUI.main_ui.run_info+="虚存空间不足，高级调度退出\n";
				}
				else //PCB池空间不足
					MainUI.main_ui.run_info+="PCB池空间不足，高级调度退出\n";
			}
		}
		else  //后备队列中没有作业
			MainUI.main_ui.run_info+="在后备队列中没有检测到作业，高级调度退出\n";
	}
	
	public void MiddleLevelScheduling()
	{
		//中级调度
		MainUI.main_ui.run_info+="系统时间："+kernel.SYSTEM_TIME+"中级调度--";
		//将挂起队列中的进程全部取出
		for(int i=0;i<ProcessModule.process_module.suspend_queue.size();i++)
		{
			PCB t=ProcessModule.process_module.suspend_queue.get(i);
			ProcessModule.process_module.TransferProcessToReadyQueue(t,true); 	//将该进程加入到就绪队列
		}
		//第一步，获取当前内存中可用的页框数
		MainUI.main_ui.run_info+="检测当前内存中可用的页框数--";
		int free_page_num=PageModule.page_module.GetFreePageNumInMemory();
		//第二步，如果可用页框数小于10，则进行中级调度
		if(free_page_num<10)
		{
			MainUI.main_ui.run_info+="当前可用页框数为："+free_page_num+"，立即进行中级调度--";
			//页框数小于10
			for(int j=22;j<PageModule.page_module.lru.size();j++)
			{
				int page=PageModule.page_module.lru.get(j);
				SuspendProcessWithPageNum((short) page);
			}
		}
		else //空闲页框数足够
			MainUI.main_ui.run_info+="当前可用页框数为："+free_page_num+"，退出中级调度\n";
	}
	
	public void LowLevelScheduling()
	{
		//低级调度
		MainUI.main_ui.run_info+="系统时间："+kernel.SYSTEM_TIME+"低级调度--";
		kernel.SystemTimeAdd();								//系统时间自增
		this.wait_time-=kernel.INTERRUPTION_INTERVAL;		//等待时间自减
		if(wait_time<=0)
		{
			if_wait=false;
			if(!ProcessModule.process_module.waiting_queue.isEmpty())
			{
				PCB t=ProcessModule.process_module.waiting_queue.get(0);
				MainUI.main_ui.run_info+="等待时间满，进程"+t.GetPid()+"退出等待！\n";
				if(t.if_in_p==true)
				{
					t.if_in_p=false;
					if(t.if_p_success)
					{
						t.AddCurrentInstructionNo();
						t.if_p_success=false;
					}
				}
				else
					t.AddCurrentInstructionNo();
				t.ins_runtime=0;
				if_wait=false;
				t.SetRuntime(0); 		//刷新已运行时间
				t.RefreshCounter(); 	//刷新时间片余额
				ProcessModule.process_module.TransferProcessToReadyQueue(t,false);
			}
		}
		//第一步，检测当前是否正处于等待态，如果处于，则CPU等待
		if(if_wait==true)
		{
			MainUI.main_ui.run_info+="当前CPU处于等待态，继续等待，退出低级调度！\n";
			PCB t=ProcessModule.process_module.waiting_queue.get(0);
			t.AddRuntime(); 		//模拟已经运行过一次
			t.AddTotalRuntime(); 	//总运行时间增加
			t.ins_runtime+=kernel.INTERRUPTION_INTERVAL;
			//处于等待态，则检查等待时间
			return;
		}
		//第二步，刷新active和expired指针
		ProcessModule.process_module.RefreshActiveExpired();
		//第三步，查看当前运行队列是否还有进程
		if(ProcessModule.process_module.IsRunningQueueEmpty()==true)
		{
			MainUI.main_ui.run_info+="运行队列中没有进程，进行重新调度--";
			for(int i=0;i<140;i++)
			{
				if(ProcessModule.process_module.IsRunningQueueEmpty()==false)
					break;
				if(!ProcessModule.process_module.ready_queue[ProcessModule.process_module.GetActivePoint()][i].isEmpty())
				{
					PCB t=ProcessModule.process_module.ready_queue[ProcessModule.process_module.GetActivePoint()][i].get(0);
					ProcessModule.process_module.TransferProcessToRunningQueue(t);
					MainUI.main_ui.run_info+="调入进程"+t.GetPid()+"--";
				}
			}
		}
		else
			MainUI.main_ui.run_info+="队列中有进程运行--";
		
		//第四步，检测当前正准备执行的指令所在的页是否在内存中
		if(ProcessModule.process_module.running_queue.isEmpty())
		{
			MainUI.main_ui.run_info+="当前没有可运行进程！\n";
			return;
		}
		PCB run=ProcessModule.process_module.running_queue.get(0);
		//CPU恢复现场
		CPU.cpu.Recovery(run);
		//检测是否发生缺页中断
		MainUI.main_ui.run_info+="运行指令--";
		if(ProcessModule.process_module.IfPageInMemory(run.page_table[run.GetCurrentInstructionNo()/(kernel.SINGLE_PAGE_SIZE/kernel.SINGLE_INSTRUCTION_SIZE)][1])==false)
		{
			//如果所需要的页面不在内存中，则发生缺页中断
			//缺页中断的处理
			MainUI.main_ui.run_info+="发生缺页中断--当前缺页号："+run.GetCurrentInstructionNo()/(kernel.SINGLE_PAGE_SIZE/kernel.SINGLE_INSTRUCTION_SIZE)+"-"+run.page_table[run.GetCurrentInstructionNo()/(kernel.SINGLE_PAGE_SIZE/kernel.SINGLE_INSTRUCTION_SIZE)][1]+"，";
			ProcessModule.process_module.SolveMissingPage(run, run.page_table[run.GetCurrentInstructionNo()/(kernel.SINGLE_PAGE_SIZE/kernel.SINGLE_INSTRUCTION_SIZE)][1]);
			MainUI.main_ui.run_info+="已进行调页处理--";
			MainUI.main_ui.run_info+="当前页表项："+run.GetCurrentInstructionNo()/(kernel.SINGLE_PAGE_SIZE/kernel.SINGLE_INSTRUCTION_SIZE)+"-"+run.page_table[run.GetCurrentInstructionNo()/(kernel.SINGLE_PAGE_SIZE/kernel.SINGLE_INSTRUCTION_SIZE)][1]+"\n";
			PageModule.page_module.LRUVisitOnePage(run.page_table[run.GetCurrentInstructionNo()/(kernel.SINGLE_PAGE_SIZE/kernel.SINGLE_INSTRUCTION_SIZE)][1]);
		}
		else
		{
			MainUI.main_ui.run_info+="没有发生缺页中断--当前访问页表："+run.GetCurrentInstructionNo()/(kernel.SINGLE_PAGE_SIZE/kernel.SINGLE_INSTRUCTION_SIZE)+"-"+run.page_table[run.GetCurrentInstructionNo()/(kernel.SINGLE_PAGE_SIZE/kernel.SINGLE_INSTRUCTION_SIZE)][1]+"\n";
			PageModule.page_module.LRUVisitOnePage(run.page_table[run.GetCurrentInstructionNo()/(kernel.SINGLE_PAGE_SIZE/kernel.SINGLE_INSTRUCTION_SIZE)][1]);
		}
		//MMU的地址变换，取指令
		MainUI.main_ui.run_info+="MMU进行地址变换--PC指针：0x"+String.format("%04x", CPU.cpu.GetPC()).toUpperCase()+
				"H，转换后的实地址：0x"+String.format("%04x", CPU.cpu.mm.VirtualAddressToRealAddress(run, (short) CPU.cpu.GetPC())).toUpperCase()+"H\n";
		//执行指令
		if(run.getInstructions().size()<=run.GetCurrentInstructionNo())
		{
			MainUI.main_ui.run_info+="\n进程"+run.GetPid()+"运行完毕！\n";
			run.ProcessCancel();		//调用进程撤销原语
			return;
		}
		int current_ins_type=kernel.GetInstructionType(run.getInstructions().get(run.GetCurrentInstructionNo()));
		MainUI.main_ui.run_info+="当前执行指令："+run.getInstructions().get(run.GetCurrentInstructionNo())+"--";
		//每一种指令的不同运行过程
		run.RefreshTimeslice(); 	//刷新时间片
		run.SetCounter(0); 			//设置已用时间片
		CPU.cpu.mm.ClearTLB(); 		//清空快表
		switch(current_ins_type)
		{
		case 1:
			MainUI.main_ui.run_info+="指令类型：系统调用--";
			ProcessModule.process_module.RunType1(run);
			break;
		case 2:
			MainUI.main_ui.run_info+="指令类型：P指令--";
			ProcessModule.process_module.RunType2(run);
			break;
		case 3:
			MainUI.main_ui.run_info+="指令类型：V指令--";
			ProcessModule.process_module.RunType3(run);
			break;
		case 4:
			MainUI.main_ui.run_info+="指令类型：申请资源--";
			ProcessModule.process_module.RunType4(run);
			break;
		case 5:
			MainUI.main_ui.run_info+="指令类型：释放资源--";
			ProcessModule.process_module.RunType5(run);
			break;
		case 6:
			MainUI.main_ui.run_info+="指令类型：普通指令--";
			ProcessModule.process_module.RunType6(run);
			break;
		}
		//检测是否运行完毕，如果运行完毕，调用撤销原语
		if(ProcessModule.process_module.IfRunOver(run)==true)
		{
			MainUI.main_ui.run_info+="\n进程"+run.GetPid()+"运行完毕！\n";
			run.ProcessCancel();		//调用进程撤销原语
		}
		//检测时间片是否用完，如果完毕，则进入就绪队列
		if(ProcessModule.process_module.IfTimeSliceOver(run)==true&&this.if_wait!=true)
		{
			MainUI.main_ui.run_info+="\n进程"+run.GetPid()+"时间片到期！\n";
			//设置counter为0
			run.SetCounter(0);
			run.SetRuntime(0);
			//动态调整优先级
			run.RefreshPriority();
			//将该进程放入就绪队列
			ProcessModule.process_module.TransferProcessToReadyQueue(run, false);
		}
		MainUI.main_ui.run_info+="\n";
	}
}

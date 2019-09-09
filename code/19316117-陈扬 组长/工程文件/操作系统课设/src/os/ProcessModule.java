package os;
import java.util.ArrayList;

import ui.MainUI;

public class ProcessModule
{
	public static ProcessModule process_module=new ProcessModule();
	
	public ArrayList<PCB> all_queue=new ArrayList<PCB>();	//所有进程链表
	public ArrayList<PCB> running_queue=new ArrayList<PCB>();	//运行队列
	@SuppressWarnings("unchecked")
	public ArrayList<PCB> [][]ready_queue=new ArrayList[2][140];	//就绪队列
	private int active=0;	//active指针
	private int expired=1;	//expired指针
	private boolean []ready_queue_bitmap=new boolean[140];			//就绪队列位图，范围 0 - 139，共140位
	public ArrayList<PCB> waiting_queue=new ArrayList<PCB>();		//等待队列
	public ArrayList<PCB> suspend_queue=new ArrayList<PCB>();		//挂起队列
	public ArrayList<PCB> end_queue=new ArrayList<PCB>();			//已经运行结束的进程
	
	private boolean[] PCB_pool_usage=new boolean[kernel.MEMORY_KERNEL_PCB_POOL_SIZE/(kernel.SINGLE_PAGE_SIZE/2)];		//PCB池使用情况
	
	ProcessModule()
	{
		for(int i=0;i<kernel.MEMORY_KERNEL_PCB_POOL_SIZE/(kernel.SINGLE_PAGE_SIZE/2);i++)
			PCB_pool_usage[i]=false;	//未被占用
		//初始化就绪队列
		for(int i=0;i<2;i++)
			for(int j=0;j<140;j++)
				ready_queue[i][j]=new ArrayList<PCB>();
	}
	
	public PCB TurnJCBToPCB(JCB jcb)
	{
		//将JCB变换为PCB
		PCB pcb=new PCB();
		//进程标识符
		pcb.SetPid(jcb.GetJobid());
		//进程状态
		pcb.SetState(kernel.PROCESS_READY);
		//进程优先级
		pcb.SetPriority(jcb.GetPriority());
		//作业创建时间
		pcb.SetJobIntime(pcb.GetJobIntime());
		//进程创建时间
		pcb.SetProcessIntime(kernel.SYSTEM_TIME);
		//作业/进程结束时间
		pcb.SetEndTime(-1); 		//在创建时不设置，等到进程撤销时设置
		//时间片长度
		pcb.RefreshTimeslice();
		//进程已经运行时间
		pcb.SetRuntime(0);
		//该进程处于运行状态下的时间片余额
		pcb.SetCounter(-1);		//在创建时不设置
		//程序状态字
		pcb.SetPSW(kernel.PSW_USER_STATE);
		//当前运行到的指令编号
		pcb.SetCurrentInstructionNo((short) 0);
		//该进程总共包含的指令数目
		pcb.SetInstructionNum(jcb.GetInstruction_num());
		//该进程所占用的页面数目
		pcb.SetPagesNum((short) (jcb.GetPagesNum()-1));
		//初始化页表
		for(short i=0;i<(kernel.MEMORY_USER_SPACE_SIZE)/kernel.SINGLE_PAGE_SIZE;i++)
		{
			pcb.EditPageTable(i,(short)-1,(short)-1);
		}
		//添加所有指令
		for(int i=0;i<pcb.GetInstruction_num();i++)
		{
			pcb.AddInstruction(jcb.GetAll_Instructions().get(i));
		}
		//所在页号
		pcb.SetInPageNum((short) -1);		//由void AddToPCBPool(PCB pcb)函数修改
		return pcb;
	}
	
	public void TransferJobCodeToSwapArea(JCB jcb,String apply)
	{
		//将指定作业的程序段存入虚存中
		//jcb为作业控制块，apply为申请到的虚存空间分配字符串
		int already_transfer=0;		//已经转移的页面数量
		short i=0;
		while(already_transfer!=jcb.GetProcessNeedPage())
		{
			if(apply.charAt(i)=='1')
			{
				PageModule.page_module.CopyPage((short)(jcb.GetInPageNum()+1+already_transfer), i);
				i++;
				already_transfer++;		//已经分配完一页
			}
			else
			{
				i++;
				continue;
			}
		}
	}
	
	public void WriteProcessPageTable(PCB pcb,String apply)
	{
		//将申请到的虚存页面写入到进程的页表中
		short count=0;
		for(int i=0;i<apply.length();i++)
		{
			if(apply.charAt(i)=='1')
			{
				pcb.EditPageTable(count, count, (short) i);
				count++;
			}
		}
	}
	
	public short GetFreePCBNumInPool()
	{
		//获取PCB池中可用的PCB数量
		short count=0;
		for(int i=0;i<kernel.MEMORY_KERNEL_PCB_POOL_SIZE/(kernel.SINGLE_PAGE_SIZE/2);i++)
			if(this.PCB_pool_usage[i]==false)
				count++;
		return count;
	}
	
	public void DeletePCBInPool(PCB pcb)
	{
		//将某一个PCB从PCB池中删除
		PCB_pool_usage[pcb.GetPoolLocation()]=false;
	}
	
	public short ApplyOnePCBInPool()
	{
		//在PCB池中申请一个PCB
		for(short i=0;i<kernel.MEMORY_KERNEL_PCB_POOL_SIZE/(kernel.SINGLE_PAGE_SIZE/2);i++)
			if(this.PCB_pool_usage[i]==false)
			{
				this.PCB_pool_usage[i]=true;
				return i;
			}
		return -1;
	}
	
	public void AddToPCBPool(PCB pcb)
	{
		//将PCB加入到PCB池中
		this.all_queue.add(pcb);		//加入到所有进程队列
		short pool_num=ApplyOnePCBInPool();		//向PCB池中申请一个PCB，获取PCB序号
		pcb.SetPoolLocation((short) (pool_num));		//设置该PCB所在的具体页号
		pcb.SetInPageNum((short) (1+pool_num/2));		//设置PCB所在的页号
		pcb.WritePCBToMemory();		//PCB内容写入内核区
	}
	
	public void TransferProcessToRunningQueue(PCB pcb)
	{
		//将进程移入运行队列
		//遍历就绪队列、等待队列、挂起队列，将进程移出，只加入到运行队列
		
		//移入运行队列
		running_queue.add(pcb);
		
		//遍历就绪队列删除
		for(int i=0;i<2;i++)
		{
			for(int j=0;j<140;j++)
			{
				ready_queue[i][j].remove(pcb);
			}
		}
		//遍历等待队列删除
		waiting_queue.remove(pcb);
		//遍历挂起队列删除
		suspend_queue.remove(pcb);
		
		//设置进程状态
		pcb.SetState(kernel.PROCESS_RUNNING);
	}
	
	public void TransferProcessToReadyQueue(PCB pcb,boolean if_active)
	{
		//将进程移入就绪队列
		//遍历运行队列、等待队列、挂起队列，将进程移出，只加入到就绪队列
		
		//移入就绪队列
		if(if_active==true)
			ready_queue[active][(pcb.GetPriority()+20)*3].add(pcb);
		else
			ready_queue[expired][(pcb.GetPriority()+20)*3].add(pcb);
		
		//遍历等待队列删除
		waiting_queue.remove(pcb);
		//遍历挂起队列删除
		suspend_queue.remove(pcb);
		//遍历运行队列删除
		running_queue.remove(pcb);
		
		//设置进程状态
		pcb.SetState(kernel.PROCESS_READY);
	}
	
	public void TransferProcessToWaitQueue(PCB pcb)
	{
		//将进程移入等待队列
		//遍历运行队列、就绪队列、挂起队列，将进程移出，只加入到等待队列
		
		//移入等待队列
		waiting_queue.add(pcb);
		//遍历就绪队列删除
		for(int i=0;i<2;i++)
		{
			for(int j=0;j<140;j++)
			{
				ready_queue[i][j].remove(pcb);
			}
		}
		//遍历运行队列删除
		running_queue.remove(pcb);
		//遍历挂起队列删除
		suspend_queue.remove(pcb);
		
		//设置进程状态
		pcb.SetState(kernel.PROCESS_WAITING);
	}
	
	public void TransferProcessToSuspendQueue(PCB pcb)
	{
		//将进程移入挂起队列
		//遍历运行队列、就绪队列、等待队列，将进程移出，只加入到挂起队列
		
		//移入挂起队列
		suspend_queue.add(pcb);
		
		//遍历就绪队列删除
		for(int i=0;i<2;i++)
		{
			for(int j=0;j<140;j++)
			{
				ready_queue[i][j].remove(pcb);
			}
		}
		//遍历运行队列删除
		running_queue.remove(pcb);
		//遍历等待队列删除
		waiting_queue.remove(pcb);
		
		//设置进程状态
		pcb.SetState(kernel.PROCESS_WAITING);
	}
	
	public void TransferProcessToEndQueue(PCB pcb)
	{
		//将进程移入完成队列
		//遍历运行队列、就绪队列、等待队列、挂起队列，将进程移出，加入到完成队列
		
		//移入完成队列
		end_queue.add(pcb);
		
		//遍历就绪队列删除
		for(int i=0;i<2;i++)
		{
			for(int j=0;j<140;j++)
			{
				ready_queue[i][j].remove(pcb);
			}
		}
		//遍历运行队列删除
		running_queue.remove(pcb);
		//遍历等待队列删除
		waiting_queue.remove(pcb);
		//遍历挂起队列删除
		suspend_queue.remove(pcb);
		
		//设置进程状态
		pcb.SetState(kernel.PROCESS_SUSPENSION);
	}
	
	public void RefreshReadyQueueBitmap()
	{
		//刷新就绪队列的bitmap
		for(int i=0;i<140;i++)
		{
			ready_queue_bitmap[i]=!ready_queue[active][i].isEmpty();
		}
	}
	
	public void RefreshActiveExpired()
	{
		//刷新active和expired指针
		boolean flag=true;
		for(int i=0;i<140;i++)
		{
			if(ready_queue[0][i].isEmpty()==false)		//该队列中还有进程
			{
				flag=false;
				break;
			}
		}
		if(flag==true)	//队列0为空
		{
			active=1;
			expired=0;
		}
		else
		{
			active=0;
			expired=1;
		}
	}
	
	public boolean IfPageInMemory(short page_num)
	{
		//检测需要的页是否在内存中
		//检测是否发生缺页中断
		if(page_num>=kernel.MEMORY_KERNEL_SPACE_SIZE/kernel.SINGLE_PAGE_SIZE&&page_num<kernel.MEMORY_SIZE/kernel.SINGLE_PAGE_SIZE)
			return true;
		else
			return false;
	}
	
	public void ChangePageTable(short ori_page_num,short changed_page_num)
	{
		//将持有原来页的进程的页表更新
		for(int i=0;i<all_queue.size();i++)
		{
			PCB t=all_queue.get(i);
			for(short j=0;j<(kernel.MEMORY_USER_SPACE_SIZE)/kernel.SINGLE_PAGE_SIZE;j++)
			{
				if(t.CheckPageTable(j)==ori_page_num)
					t.EditPageTable(j, j, changed_page_num);
				if(t.CheckPageTable(j)==changed_page_num)
					t.EditPageTable(j, j, ori_page_num);
			}
		}
	}
	
	public void SolveMissingPage(PCB pcb,short need_page_num)
	{
		//缺页中断的处理，need_page_num为需要的在外存中的页的页号
		if(PageModule.page_module.GetFreePageNumInMemory()<1)		//内存满了
		{
			int out_page_num=PageModule.page_module.LRUGetLastPageNum();	//LRU进行页面置换
			PageModule.page_module.ExchangePage((short) out_page_num, need_page_num);	//页面置换
			ChangePageTable((short) out_page_num,need_page_num);	//更新对应的进程的页表
		}
		else		//内存没有满
		{
			short in_page_num=PageModule.page_module.GetOneFreePageInMemory();	//在内存中申请一页
			PageModule.page_module.MoveToMemory(in_page_num, need_page_num); 	//将交换区的页移入
			PageModule.page_module.RecyclePage(need_page_num); 					//回收交换区
			ChangePageTable(in_page_num,need_page_num);							//更新页表
		}
	}
	
	public boolean IfRunOver(PCB pcb)
	{
		//检测某进程是否运行完毕
		if((pcb.GetTotalRunTime()>=pcb.GetTotalNeedTime())&&(pcb.GetCurrentInstructionNo()>=pcb.getInstructions().size()))
			return true;
		else
			return false;
	}
	
	public boolean IfTimeSliceOver(PCB pcb)
	{
		//检测时间片是否用完
		if(pcb.GetCounter()<=0)
			return true;
		else
			return false;
	}
	
	public void AddToEndQueue(PCB pcb)
	{
		//将作业加入到结束队列
		this.end_queue.add(pcb);
	}
	
	public boolean IsRunningQueueEmpty()
	{
		//检测运行队列是否为空
		return this.running_queue.isEmpty();
	}
	
	public boolean IsReadyQueueEmpty()
	{
		//检测就绪队列是否为空
		boolean if_empty=true;
		for(int i=0;i<2;i++)
			for(int j=0;j<140;j++)
				if(!this.ready_queue[i][j].isEmpty())
					if_empty=false;
		return if_empty;
	}
	
	public boolean IsWaitQueueEmpty()
	{
		//检测等待队列是否为空
		return waiting_queue.isEmpty();
	}
	
	public boolean IsSuspendQueueEmpty()
	{
		//检测挂起队列是否为空
		return suspend_queue.isEmpty();
	}

	public int GetActivePoint()
	{
		//获取active指针
		return active;
	}
	public int GetExpiredPoint()
	{
		//获取expired指针
		return expired;
	}
	
	public PCB GetPCBWithID(short id)
	{
		//根据进程ID获取PCB
		for(int i=0;i<all_queue.size();i++)
			if(all_queue.get(i).GetPid()==id)
				return all_queue.get(i);
		return null;
	}
	
	public void RunType1(PCB pcb)
	{
		//类型1指令的处理
		Scheduling.sch.if_wait=true;
		Scheduling.sch.wait_time=kernel.GetInstructionTime(pcb.getInstructions().get(pcb.GetCurrentInstructionNo()));
		pcb.SetPSW(kernel.PSW_KERNEL_STATE); 	//刷新PSW为目态
		MainUI.main_ui.run_info+="PSW：核心态\n";
		pcb.ProcessWait(); 		//调用进程原语
	}
	
	public void RunType2(PCB pcb)
	{
		//类型2指令的处理
		Scheduling.sch.if_wait=true;
		Scheduling.sch.wait_time=kernel.GetInstructionTime(pcb.getInstructions().get(pcb.GetCurrentInstructionNo()));
		pcb.SetPSW(kernel.PSW_KERNEL_STATE); 	//刷新PSW为目态
		MainUI.main_ui.run_info+="PSW：核心态\n";
		pcb.ProcessWait(); 		//调用进程原语
		
		pcb.if_in_p=true;
		MainUI.main_ui.run_info+="当前P信号量值："+kernel.MUTEX[pcb.getInstructions().get(pcb.GetCurrentInstructionNo())%10]+"--";
		if(DeadLock.dl.Process_P_Mutex(pcb,pcb.getInstructions().get(pcb.GetCurrentInstructionNo())%10)==false)
		{
			//无法P信号量
			MainUI.main_ui.run_info+="P失败，等待下次尝试\n";
			pcb.RefreshPriority();		//P失败，则更新优先级，让权
			pcb.if_p_success=false;
		}
		else
		{
			//P成功
			pcb.if_p_success=true;
			MainUI.main_ui.run_info+="P成功，此时P信号量值："+kernel.MUTEX[pcb.getInstructions().get(pcb.GetCurrentInstructionNo())%10]+"\n";
		}
	}
	
	public void RunType3(PCB pcb)
	{
		//指令类型3的处理
		Scheduling.sch.if_wait=true;
		Scheduling.sch.wait_time=kernel.GetInstructionTime(pcb.getInstructions().get(pcb.GetCurrentInstructionNo()));
		pcb.SetPSW(kernel.PSW_KERNEL_STATE); 	//刷新PSW为目态
		MainUI.main_ui.run_info+="PSW：核心态\n";
		pcb.ProcessWait(); 		//调用进程原语
		
		DeadLock.dl.Process_V_Mutex(pcb, pcb.getInstructions().get(pcb.GetCurrentInstructionNo())%10);
		MainUI.main_ui.run_info+="当前V信号量值："+kernel.MUTEX[pcb.getInstructions().get(pcb.GetCurrentInstructionNo())%10]+"\n";
	}
	
	public void RunType4(PCB pcb)
	{
		//指令类型4的处理
		Scheduling.sch.if_wait=true;
		Scheduling.sch.wait_time=kernel.GetInstructionTime(pcb.getInstructions().get(pcb.GetCurrentInstructionNo()));
		pcb.SetPSW(kernel.PSW_KERNEL_STATE); 	//刷新PSW为目态
		MainUI.main_ui.run_info+="PSW：核心态\n";
		pcb.ProcessWait(); 		//调用进程原语
		
		pcb.if_in_p=true;
		MainUI.main_ui.run_info+="当前申请资源的剩余数量："+DeadLock.dl.GetResourceNum(pcb.getInstructions().get(pcb.GetCurrentInstructionNo())%10)+"--";
		if(DeadLock.dl.Process_Apply_Resource(pcb, pcb.getInstructions().get(pcb.GetCurrentInstructionNo())%10)==false)
		{
			//无法申请到资源
			MainUI.main_ui.run_info+="申请资源失败，等待下次尝试\n";
			pcb.RefreshPriority();		//申请资源失败，则更新优先级，让权
			pcb.if_p_success=false;
		}
		else
		{
			//可以申请资源
			pcb.if_p_success=true;
			MainUI.main_ui.run_info+="申请资源成功，此时资源数量："+DeadLock.dl.GetResourceNum(pcb.getInstructions().get(pcb.GetCurrentInstructionNo())%10)+"\n";
		}
	}
	
	public void RunType5(PCB pcb)
	{
		//指令类型5的处理
		Scheduling.sch.if_wait=true;
		Scheduling.sch.wait_time=kernel.GetInstructionTime(pcb.getInstructions().get(pcb.GetCurrentInstructionNo()));
		pcb.SetPSW(kernel.PSW_KERNEL_STATE); 	//刷新PSW为目态
		MainUI.main_ui.run_info+="PSW：核心态\n";
		pcb.ProcessWait(); 		//调用进程原语
		
		DeadLock.dl.Process_Return_Resource(pcb, pcb.getInstructions().get(pcb.GetCurrentInstructionNo())%10);
		MainUI.main_ui.run_info+="当前资源量值："+DeadLock.dl.GetResourceNum(pcb.getInstructions().get(pcb.GetCurrentInstructionNo())%10)+"\n";
	}
	
	public void RunType6(PCB pcb)
	{
		//类型6指令的处理
		pcb.AddRuntime(); 		//模拟已经运行过一次
		pcb.AddTotalRuntime(); 	//总运行时间增加
		pcb.ins_runtime+=kernel.INTERRUPTION_INTERVAL;
		//根据总运行时间刷新当前的指令
		if(pcb.ins_runtime==kernel.GetInstructionTime(pcb.getInstructions().get(pcb.GetCurrentInstructionNo())))
		{
			pcb.AddCurrentInstructionNo();
			pcb.ins_runtime=0;
		}
		pcb.RefreshCounter(); 	//刷新时间片余额
		pcb.SetPSW(kernel.PSW_USER_STATE); 	//刷新PSW为目态
		MainUI.main_ui.run_info+="PSW：用户态\n";
	}
}

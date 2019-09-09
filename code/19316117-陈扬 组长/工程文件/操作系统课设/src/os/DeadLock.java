package os;

import java.util.ArrayList;

public class DeadLock
{
	public static DeadLock dl=new DeadLock();	//该类的静态对象
	@SuppressWarnings("unchecked")
	public ArrayList<PCB> []PV_apply=new ArrayList[10];	//PV信号量当前的占用情况（P过该资源但是没有释放的进程）
	private int m=10;	//向量长度m，每类资源中可供分配的资源数目
	private int n=500;	//向量长度n，进程个数
	
	public int []Available=new int[m];			//每类资源中可供分配的资源数目
	public int [][]Allocation=new int[n][m];	//已分配给每个进程的每类资源数目
	public int [][]Request=new int[n][m];		//每个进程对每类资源的申请数目
	private int []Work=new int[m];				//长度为m的工作向量
	private boolean []finish=new boolean[n];	//长度为n的布尔型向量
	
	DeadLock()
	{
		for(int i=0;i<10;i++)
			PV_apply[i]=new ArrayList<PCB>();
		InitAvailable();
		InitAllocation();
		InitRequest();
		ResetWork();
		InitFinish();
	}
	
	public void InitAvailable()
	{
		//初始化Available向量
		for(int i=0;i<m;i++)
			Available[i]=kernel.SYSTEM_RESOURCE[i];
	}
	
	public void InitAllocation()
	{
		//初始化Allocation向量
		for(int i=0;i<n;i++)
			for(int j=0;j<m;j++)
				Allocation[i][j]=0;
	}
	
	public void InitRequest()
	{
		//初始化Request向量
		for(int i=0;i<n;i++)
			for(int j=0;j<m;j++)
				Request[i][j]=0;
	}
	
	public void ResetWork()
	{
		//设置Work向量
		for(int i=0;i<m;i++)
			Work[i]=Available[i];
	}
	
	public void InitFinish()
	{
		//初始化finish向量
		for(int i=0;i<n;i++)
			finish[i]=true;
	}
	
	public boolean Process_P_Mutex(PCB pcb,int num)
	{
		//某个进程P第num个mutex信号量，只有成功了才将其放入apply队列
		//返回值为能够P成功
		if(kernel.MUTEX[num]<=0)		//当前没有资源，不能P
			return false;
		else		//有资源，可以P
		{
			kernel.MUTEX[num]--;
			this.PV_apply[num].add(pcb);
			return true;
		}
	}
	
	public void Process_V_Mutex(PCB pcb,int num)
	{
		//某个进程V第num个mutex信号量
		kernel.MUTEX[num]++;		//释放资源
		this.PV_apply[num].remove(pcb);		//将pcb移出
	}
	
	public boolean Process_Apply_Resource(PCB pcb,int num)
	{
		//某个进程申请某个资源
		//返回值为是否能够申请成功
		if(Available[num]<=0)		//当前没有资源，不能申请
		{
			Request[pcb.GetPid()][num]++;		//在Request数组中进行记录
			return false;
		}
		else		//当前有资源，可以申请
		{
			Available[num]--;		//分配资源
			Allocation[pcb.GetPid()][num]++;
			return true;
		}
	}
	
	public int GetResourceNum(int num)
	{
		//获取某类资源的数目
		return Available[num];
	}
	
	public void Process_Return_Resource(PCB pcb,int num)
	{
		//某个进程归还资源
		Available[num]++;
		Allocation[pcb.GetPid()][num]--;
	}
	
	public boolean IfAllocationLineEmpty(int k)
	{
		//判断Allocation向量的第k行是否为空
		boolean empty=true;
		for(int i=0;i<m;i++)
			if(Allocation[k][i]!=0)
				empty=false;
		return empty;
	}
	
	public boolean Step3_find_k_value(int k)
	{
		//第3步，找到符合条件的k值
		//满足条件：finish[k]==false && Request[k , *] <= Work[*]
		//判断当前输入的k值是否符合条件
		boolean if_ok=true;
		for(int i=0;i<m;i++)
		{
			if(!(Request[k][i]<=Work[i]))
				if_ok=false;
		}
		return (finish[k]==false)&&if_ok;
	}
	
	public ArrayList<PCB> CheckDeadLock()
	{
		//死锁检测
		
		//第一步，令Work[*]=Available[*]
		ResetWork();
		InitFinish();
		//第二步，如果Allocation[k,*]不等于0，令finish[k]=false;否则finish[k]=true
		for(int k=0;k<n;k++)
		{
			if(!IfAllocationLineEmpty(k))
				finish[k]=false;
			else
				finish[k]=true;
		}
		//第三步，寻找一个k值
		for(int k=0;k<n;k++)
		{
			if(Step3_find_k_value(k)==true)
				//第四步，修改Work[*]=Work[*]+Allocation[k,*],finish[k]=true
			{
				for(int j=0;j<m;j++)
					Work[j]=Work[j]+Allocation[k][j];
				finish[k]=true;
				k=0;
			}
		}
		//第五步，查找处于死锁的进程
		ArrayList<PCB> dl_pcb=new ArrayList<PCB>();
		for(int k=0;k<n;k++)
		{
			if(finish[k]==false)
				dl_pcb.add(ProcessModule.process_module.GetPCBWithID((short) k));
		}
		return dl_pcb;
	}
}

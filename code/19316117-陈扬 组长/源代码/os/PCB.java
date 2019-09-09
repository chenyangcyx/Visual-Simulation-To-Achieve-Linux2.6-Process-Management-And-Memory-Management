package os;
import java.util.ArrayList;
import java.util.Random;

public class PCB
{
	private short pid;		//进程标识符
	private short state;	//进程状态。就绪态、等待态、运行态、挂起态
	private short priority;	//进程优先级
	private int job_intime;	//作业创建时间
	private int process_intime;	//进程创建时间
	private int end_time;	//作业/进程结束时间
	private short timeslice;	//时间片长度
	private int runtime;	//每次运行时，进程已经运行时间
	private int counter;	//该进程处于运行状态下的时间片余额
	private byte PSW;		//程序状态字。管态、目态
	private short current_instruction_no;	//当前运行到的指令编号
	private short instruction_num;	//该进程总共包含的指令数目
	private short pages_num;	//该作业/进程所占用的页面数目
	public short [][]page_table=new short[(kernel.MEMORY_USER_SPACE_SIZE)/kernel.SINGLE_PAGE_SIZE][2];
	//页表，page_table[i][0]为进程的页号，从0开始编号；page_table[i][1]为对应的物理页号
	private ArrayList<Integer> instructions=new ArrayList<Integer>();	//该进程所有的指令
	
	private short in_page_num=0;		//该PCB所在的页号
	private short pool_location=-1;		//该PCB在PCB池的位置
	private int total_runtime=0;		//总共运行的时间
	public int ins_runtime=0;			//指令的执行总时间
	public boolean if_in_p=false;		//是否处于P状态
	public boolean if_p_success=true;	//P是否成功
	
	public void ProcessCreate()
	{
		//进程原语：进程创建
		//将该进程设置为就绪态，加入到就绪队列
		ProcessModule.process_module.all_queue.add(this);
		this.state=kernel.PROCESS_READY;
		ProcessModule.process_module.TransferProcessToReadyQueue(this,true);
	}
	
	public void ProcessCancel()
	{
		//进程原语：进程撤销
		//设置进程结束时间、移入完成队列
		this.end_time=kernel.SYSTEM_TIME;
		ProcessModule.process_module.TransferProcessToEndQueue(this);
		//清空页表
		for(int i=0;i<(kernel.MEMORY_USER_SPACE_SIZE)/kernel.SINGLE_PAGE_SIZE;i++)
		{
			page_table[i][0]=-1;
			page_table[i][1]=-1;
		}
		//释放该进程占有的所有页
		for(int i=0;i<(kernel.MEMORY_USER_SPACE_SIZE)/kernel.SINGLE_PAGE_SIZE;i++)
		{
			if(this.page_table[i][1]!=-1)
				PageModule.page_module.RecyclePage(this.page_table[i][1]);
		}
		//释放进程占用的所有PV信号量和资源
		//释放PV信号量
		for(int i=0;i<10;i++)
		{
			if(DeadLock.dl.PV_apply[i].contains(this)==true)		//如果该PCB申请了资源
			{
				DeadLock.dl.PV_apply[i].remove(this);	//将该进程移出
				kernel.MUTEX[i]++;
			}
		}
		//释放申请的资源
		for(int i=0;i<10;i++)
		{
			if(DeadLock.dl.Allocation[this.GetPid()][i]>=0)
			{
				DeadLock.dl.Available[i]+=DeadLock.dl.Allocation[this.GetPid()][i];
				DeadLock.dl.Allocation[this.GetPid()][i]=0;
				DeadLock.dl.Request[this.GetPid()][i]=0;
			}
		}
		//将该PCB从PCB池中删除
		ProcessModule.process_module.DeletePCBInPool(this);
	}
	
	public void ProcessWait()
	{
		//进程原语：进程阻塞
		//将进程加入到阻塞队列
		ProcessModule.process_module.TransferProcessToWaitQueue(this);
		this.state=kernel.PROCESS_WAITING;
	}
	
	public void ProcessSuspend()
	{
		//进程原语：进程挂起
		//将进程加入到挂起队列
		ProcessModule.process_module.TransferProcessToSuspendQueue(this);
		this.state=kernel.PROCESS_SUSPENSION;
	}
	
	public void ProcessWake()
	{
		//进程原语：进程唤醒
		//将该进程从等待态移出，加入到就绪队列
		ProcessModule.process_module.TransferProcessToReadyQueue(this,true);
		this.state=kernel.PROCESS_READY;		//修改进程状态为就绪态
	}
	
	public void RefreshPriority()
	{
		//随机生成-5~5之间的数字 更新进程的优先级
		short changed=(short) (new Random().nextInt(5-(-5))+(-5));
		this.priority+=changed;
		if(this.priority>=19)
			this.priority=19;
		if(this.priority<=-20)
			this.priority=-20;
	}
	
	public int GetTotalNeedTime()
	{
		//获取此PCB运行完成总共需要的时间
		int count=0;
		for(int i=0;i<this.GetInstruction_num();i++)
			count+=kernel.GetInstructionTime(this.getInstructions().get(i));
		return count;
	}
	
	public void RefreshTotalRuntime()
	{
		//刷新已经运行的时间
		int count=0;
		for(int i=0;i<this.GetCurrentInstructionNo()-1;i++)
		{
			count+=kernel.GetInstructionTime(this.getInstructions().get(i));
		}
		this.SetTotal_runtime(count);
	}
	
	public void RefreshTimeslice()
	{
		//根据更新后的优先级更新该进程能够获得的时间片
		if(this.priority<0)
			this.timeslice=(short) (((short)(-35*this.priority+100))/10*10);
		else
			this.timeslice=(short) (((short)(-2.6316*this.priority+100))/10*10);
	}
	
	public void AddRuntime()
	{
		//增加进程已经运行时间runtime
		this.runtime=this.runtime+kernel.INTERRUPTION_INTERVAL;
	}
	
	public void AddTotalRuntime()
	{
		//增加进程已经运行的总时间
		this.SetTotal_runtime(this.GetTotal_runtime() + kernel.INTERRUPTION_INTERVAL);
	}
	
	public void RefreshCounter()
	{
		//更新进程的时间片余额
		this.counter=this.timeslice-this.runtime;
	}
	
	public void AddCurrentInstructionNo()
	{
		//更新current_instruction_no，当前正要运行的指令编号
		this.current_instruction_no++;
	}
	
	public int GetTotalRunTime()
	{
		//获取该进程总共的运行时间
		int time=0;
		for(int i=0;i<this.current_instruction_no;i++)
			time+=kernel.GetInstructionTime(instructions.get(i));
		return time;
	}
	
	public void AddPageTable(short table_data)
	{
		//增加页表项
		int location=0;
		while(this.page_table[location][0]!=-1)
		{
			location++;
		}
		this.page_table[location][0]=(short) location;
		this.page_table[location][1]=table_data;
	}

	public void EditPageTable(short line,short data_1,short data_2)
	{
		//修改页表
		this.page_table[line][0]=data_1;
		this.page_table[line][1]=data_2;
	}
	
	public short CheckPageTable(short line_no)
	{
		//查询第line_no项表的值
		return this.page_table[line_no][1];
	}
	
	public void AddInstruction(int instruction)
	{
		//添加指令
		this.instructions.add(instruction);
	}
	
	public boolean IfPCBInUpper()
	{
		//判断该PCB是否位于页的上半段
		if(this.in_page_num/100==1)
			return true;
		else
			return false;
	}
	public short PCBStartAddress()
	{
		//获取该PCB在页中的起始地址
		if(IfPCBInUpper()==true)
			return 0;
		else
			return (short) (kernel.SINGLE_PAGE_SIZE/2);
	}
	
	public void WritePCBToMemory()
	{
		//将此PCB写入到内存的PCB池中
		Page pa=new Page(this.in_page_num);		//获取该PCB所在的页
		short base_address=PCBStartAddress();	//PCB内容的起始地址
		//PCB标识，01
		pa.SetPageData((short) (base_address+0), (short)'p');
		//进程标识符，23
		pa.SetPageData((short) (base_address+2), this.pid);
		//进程状态，45
		pa.SetPageData((short) (base_address+4), this.state);
		//进程优先级，67
		pa.SetPageData((short) (base_address+6), this.priority);
		//作业创建时间，89
		pa.SetPageData((short) (base_address+8), (short) this.job_intime);
		//进程创建时间，1011
		pa.SetPageData((short) (base_address+10), (short) this.process_intime);
		//作业/进程结束时间，1213
		pa.SetPageData((short) (base_address+12), (short) this.end_time);
		//时间片长度，1415
		pa.SetPageData((short) (base_address+14), this.timeslice);
		//进程已经运行时间，1617
		pa.SetPageData((short) (base_address+16), (short) this.runtime);
		//进程处于运行状态下的时间片余额，1819
		pa.SetPageData((short) (base_address+18), (short) this.counter);
		//程序状态字，管态、目态，2021
		pa.SetPageData((short) (base_address+20), this.PSW);
		//当前运行到的指令编号，2223
		pa.SetPageData((short) (base_address+22), this.current_instruction_no);
		//总共包含的指令数目，2425
		pa.SetPageData((short) (base_address+24), this.instruction_num);
		//进程所占页面数，2627
		pa.SetPageData((short) (base_address+26), this.pages_num);
		//页表，2829~
		for(int i=0;i<(kernel.MEMORY_USER_SPACE_SIZE)/kernel.SINGLE_PAGE_SIZE;i++)
		{
			if(base_address+28+i*2>=base_address+kernel.SINGLE_PAGE_SIZE/2)
				break;
			pa.SetPageData((short) (base_address+28+i*2), (short) ((this.page_table[i][0]<<8)&0xFF|(this.page_table[i][1]&0xFF)));
		}
	}
	
	public short GetPid() {
		return pid;
	}
	public void SetPid(short pid) {
		this.pid = pid;
	}
	public short GetState() {
		return state;
	}
	public void SetState(short state) {
		this.state = state;
	}
	public short GetPriority() {
		return priority;
	}
	public void SetPriority(short priority) {
		this.priority = priority;
	}
	public int GetJobIntime() {
		return job_intime;
	}
	public void SetJobIntime(int job_intime) {
		this.job_intime = job_intime;
	}
	public int GetProcessIntime() {
		return process_intime;
	}
	public void SetProcessIntime(int process_intime) {
		this.process_intime = process_intime;
	}
	public int GetEndTime() {
		return end_time;
	}
	public void SetEndTime(int end_time) {
		this.end_time = end_time;
	}
	public short GetTimeslice() {
		return timeslice;
	}
	public int GetRuntime() {
		return runtime;
	}
	public void SetRuntime(int runtime) {
		this.runtime = runtime;
	}
	public int GetCounter() {
		return counter;
	}
	public void SetCounter(int counter) {
		this.counter = counter;
	}
	public byte GetPSW() {
		return PSW;
	}
	public void SetPSW(byte pSW) {
		PSW = pSW;
	}
	public short GetCurrentInstructionNo() {
		return current_instruction_no;
	}
	public void SetCurrentInstructionNo(short current_instruction_no) {
		this.current_instruction_no = current_instruction_no;
	}
	public short GetInstruction_num() {
		return instruction_num;
	}
	public void SetInstructionNum(short instruction_num) {
		this.instruction_num = instruction_num;
	}
	public short GetPagesNum() {
		return pages_num;
	}
	public void SetPagesNum(short pages_num) {
		this.pages_num = pages_num;
	}
	public ArrayList<Integer> getInstructions() {
		return instructions;
	}
	public void setInstructions(ArrayList<Integer> instructions) {
		this.instructions = instructions;
	}
	public void SetInPageNum(short num)
	{
		this.in_page_num=num;
	}
	public short GetInPageNum()
	{
		return this.in_page_num;
	}

	public short GetPoolLocation() {
		return pool_location;
	}

	public void SetPoolLocation(short pool_location) {
		this.pool_location = pool_location;
	}

	public int GetTotal_runtime() {
		return total_runtime;
	}

	public void SetTotal_runtime(int total_runtime) {
		this.total_runtime = total_runtime;
	}
}

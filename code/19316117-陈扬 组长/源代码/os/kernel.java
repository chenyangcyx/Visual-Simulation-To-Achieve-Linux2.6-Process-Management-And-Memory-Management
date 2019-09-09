package os;

public class kernel
{
	/*系统基本信息*/
	public static int SINGLE_PAGE_SIZE=512;	//每一页/块的大小
	public static int MEMORY_SIZE=32*1024;	//内存大小，32KB
	public static int MEMORY_KERNEL_SPACE_SIZE=16*1024;	//内存内核空间大小，16KB
	public static int MEMORY_KERNEL_CORESTACKANDOSKERNEL_SIZE=512;	//核心栈+系统内核大小，1页
	public static int MEMORY_KERNEL_PCB_POOL_SIZE=31*512;	//PCB池大小，31页
	public static int MEMORY_USER_SPACE_SIZE=16*1024;	//内存用户空间大小（页表、页框使用），16KB
	public static int HARDDISK_SIZE=1*1024*1024;	//硬盘空间大小，1MB
	public static int HARDDISK_VIRTUAL_MEMORY_SIZE=64*1024;	//虚存区大小，128页，64KB
	public static int HARDDISK_SYSTEMFILE_SIZE=16*1024;	//系统文件大小，32页，16KB
	public static int HARDDISK_FILE_SPACE_SIZE=944*1024;	//文件区大小，1888页，944KB
	public static int HARDDISK_CYLINDER_NUM=32;	//磁盘磁道数
	public static int HARDDISK_SECTOR_NUM=64;	//磁盘扇区数
	public static int HARDDISK_PAGE_SIZE=512;	//磁盘每页/块大小
	public static int SINGLE_INSTRUCTION_SIZE=8;	//单条指令的大小
	public static int INSTRUCTIONS_PER_PAGE=SINGLE_PAGE_SIZE/SINGLE_INSTRUCTION_SIZE;	//每一页的指令数目
	
	public static int INTERRUPTION_INTERVAL=10;		//系统发生中断的间隔
	public static int SYSTEM_TIME=0;		//系统内时间
	public static void SystemTimeAdd() {kernel.SYSTEM_TIME+=kernel.INTERRUPTION_INTERVAL;}	//系统时间自增
	public static int TLB_LENGTH=kernel.MEMORY_USER_SPACE_SIZE/kernel.SINGLE_PAGE_SIZE/2;	//TLB快表的长度，16
	/*系统基本信息*/
	
	/*Process State 进程状态参数*/
	public final static short PROCESS_READY = 0;	//就绪态
	public final static short PROCESS_WAITING = 1;	//等待态
	public final static short PROCESS_RUNNING = 2;	//运行态
	public final static short PROCESS_SUSPENSION = 3;	//挂起态
	/*Process State 进程状态参数*/
	
	/*Process PSW 程序状态字*/
	public final static byte PSW_KERNEL_STATE=0;	//管态
	public final static byte PSW_USER_STATE=1; 		//目态
	/*Process PSW 程序状态字*/
	
	/*硬件初始化需要变量*/
	public static String MEMORYFILE_PATHNAME="./input/memory.dat";		//内存文件地址
	public static String HARDDISKFILE_PATHNAME="./input/harddisk.dat";	//硬盘文件地址
	public static String CPUFILE_PATHNAME="./input/cpu.dat";			//CPU文件地址
	/*硬件初始化需要变量*/
	
	/*60条指令*/
	public static int GetInstructionType(int instruction){
		//获取指令类型
		if(instruction>=0&&instruction<=9)return 1;
		if(instruction>=10&&instruction<=19)return 2;
		if(instruction>=20&&instruction<=29)return 3;
		if(instruction>=30&&instruction<=39)return 4;
		if(instruction>=40&&instruction<=49)return 5;
		if(instruction>=50&&instruction<=59)return 6;
		return -1;}
	public static int GetInstructionTime(int instruction)
	{/*获取指令执行所需要时间*/return ((int)(instruction%10))*10+20;}
	public static int[] MUTEX= {-4,-3,-2,-1,0,1,2,3,4,5};		//临界区信号量,10个
	public static int[] SYSTEM_RESOURCE= {0,1,2,3,4,5,6,7,8,9};	//系统资源量，10个
	public static int GetUseResourceNum(int instruction)
	{/*获取该指令所申请、释放的PV、资源所在的数组序号*/return instruction%10;}
	/*60条指令*/
}

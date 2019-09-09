package os;
import java.util.ArrayList;

public class JCB
{
	private short job_id;	//作业ID
	private short priority;	//作业/进程的优先级
	private int job_intime;	//作业进入时间
	private short instruction_num;	//作业包含的指令数目
	private short pages_num;	//作业所占用的页面数目
	private ArrayList<Short> all_instructions=new ArrayList<Short>();	//所有指令的链表
	
	private short in_page_num=0;		//该JCB所在的页号
	
	//计算作业所占用的页面数目
	public short CalculatePagesNum()
	{
		//每条指令占8个字节，1页有64条指令，加上JCB占1页空间
		if(this.instruction_num==0)
			return 1+1;
		else if((this.instruction_num%64==0)&&(this.instruction_num!=0))
			return (short) (this.instruction_num/64+1);
		else
			return (short) ((this.instruction_num/64+1)+1);
	}
	
	public short GetProcessNeedPage()
	{
		return (short) (this.pages_num-1);
	}
	
	public short GetPriority()
	{
		return priority;
	}
	public void SetPriority(short priority)
	{
		this.priority = priority;
	}
	public int GetJobIntime()
	{
		return this.job_intime;
	}
	public void SetJobIntime(int job_intime)
	{
		this.job_intime=job_intime;
	}
	public short GetInstruction_num()
	{
		return instruction_num;
	}
	public void SetInstructionNum(short instruction_num)
	{
		this.instruction_num = instruction_num;
	}
	public short GetPagesNum()
	{
		return pages_num;
	}
	public void SetPagesNum(short pages_num)
	{
		this.pages_num = pages_num;
	}

	public ArrayList<Short> GetAll_Instructions()
	{
		return all_instructions;
	}

	public void SetAll_Instructions(ArrayList<Short> all_instructions)
	{
		this.all_instructions = all_instructions;
	}
	public void SetInPageNum(short num)
	{
		this.in_page_num=num;
	}
	public short GetInPageNum()
	{
		return this.in_page_num;
	}
	public short GetJobid()
	{
		return job_id;
	}
	public void SetJobid(short job_id)
	{
		this.job_id = job_id;
	}
}

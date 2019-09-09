package os;
import java.io.*;
import java.util.*;
import hardware.CPU;

public class JobModule
{
	public static JobModule job_module=new JobModule();
	
	private int job_id=0;	//作业号
	private int job_page_location_start=(kernel.MEMORY_SIZE+kernel.HARDDISK_VIRTUAL_MEMORY_SIZE+kernel.HARDDISK_SYSTEMFILE_SIZE)/kernel.SINGLE_PAGE_SIZE;
	//作业存储区的地址开始
	private int job_page_location_end=(kernel.MEMORY_SIZE+kernel.HARDDISK_SIZE)/kernel.SINGLE_PAGE_SIZE;
	//作业存储区的地址结束
	private int next_to_pcb=0;	//下一个将要读入的作业序号
	private int write_job_page_num=job_page_location_start;		//写入作业的编号
	public ArrayList<JCB> job_list=new ArrayList<JCB>();		//作业后备队列
	ArrayList<JCB> all_jcb=new ArrayList<JCB>();			//存储所有JCB的数组
	
	public short GetJobNum()
	{
		//获得当前磁盘中的作业总数量
		short job_num=0;
		for(int i=this.job_page_location_start;i<this.job_page_location_end;i++)
		{
			Page temp=new Page((short) i);		//获得当前该页面
			if(temp.GetPageData((short) 0)=='j')
			{
				job_num++;		//检测是否为作业的JCB所在的页
			}
		}
		return job_num;
	}
	
	public int GetCurrentCreateJobID()
	{
		//获取当前要创建的作业的编号
		return this.job_id;
	}
	
	public void SaveJobToHardDisk(JCB jcb)
	{
		//将作业保存到外存，d1是JCB数据块，d2是包含所有指令的ArrayList，每条指令占8字节
		Page jcb_page=new Page((short) this.write_job_page_num);		//获取应写入JCB的那一页
		jcb.SetInPageNum((short) write_job_page_num); 					//设置作业所在的页号
		this.write_job_page_num++;
		jcb_page.SetPageData((short)0, (short) 'j');					//写入JCB的标识，01位
		jcb_page.SetPageData((short)2, (short) job_id);					//写入作业的序号ID，23位
		job_id++;
		jcb_page.SetPageData((short)4, jcb.GetPriority());				//写入作业优先级，45位
		jcb_page.SetPageData((short)6, (short) jcb.GetJobIntime());		//写入作业进入时间，67位
		jcb_page.SetPageData((short)8, jcb.GetInstruction_num());		//写入指令数目，89位
		jcb.SetPagesNum(jcb.CalculatePagesNum()); 						//计算该进程所需要占的页面数
		jcb_page.SetPageData((short)10, jcb.GetPagesNum());				//写入占用页面数目，1011位
		//开始随机填充接下来的部分，模拟数据区
		for(short i=12;i<kernel.SINGLE_PAGE_SIZE;i+=2)
		{
			jcb_page.SetPageData(i, (short) CPU.random.nextInt());;
		}
		
		//将所有指令写入接下来的页
		int already_write_instruction_num=0;
		for(short i=0;i<jcb.GetPagesNum()-1;i++)
		{
			Page t=new Page((short) (this.write_job_page_num+i));		//获取该页面
			//写入页的内容
			for(short j=0;j<512;j+=8)
			{
				if(already_write_instruction_num<jcb.GetInstruction_num())	//指令还未写完
				{
					//写入指令
					t.SetPageData(j, jcb.GetAll_Instructions().get(already_write_instruction_num++));
					//随机填充数据
					for(short k=(short) (j+2);k<j+kernel.SINGLE_INSTRUCTION_SIZE;k+=2)
						t.SetPageData(k, (short) CPU.random.nextInt());
				}
				else	//指令已经写完
				{
					//随机填充数据
					for(short k=(short) j;k<j+kernel.SINGLE_INSTRUCTION_SIZE;k+=2)
						t.SetPageData(k, (short) CPU.random.nextInt());
				}
			}
		}
		this.write_job_page_num+=jcb.GetPagesNum()-1;		//指针后移
	}
	
	public void GetJCBFromFile(File f)
	{
		//从文件中读取所有的JCB
		int line_count=0;
		try {
			FileReader fr = new FileReader(f);
			BufferedReader bf = new BufferedReader(fr);
			String str="";
			JCB t = null;
			while((str=bf.readLine())!=null)
			{
				switch(line_count%5)
				{
				case 0:		//job_id
					String sp[]=str.split("=");
					t=new JCB();
					t.SetJobid((short) Integer.parseInt(sp[1]));
					all_jcb.add(t);
					line_count++;
					break;
				case 1:		//priority
					String sp1[]=str.split("=");
					t.SetPriority((short) Integer.parseInt(sp1[1]));
					line_count++;
					break;
				case 2:		//instruction_num
					String sp2[]=str.split("=");
					t.SetInstructionNum((short) Integer.parseInt(sp2[1]));
					line_count++;
					break;
				case 3:		//所有指令
					String []all_instru=str.split(",");
					for(int i=0;i<all_instru.length;i++)
						t.GetAll_Instructions().add((short) Integer.parseInt(all_instru[i]));
					line_count++;
					t.SetJobIntime(kernel.SYSTEM_TIME); 		//设置作业进入时间
					t.SetPagesNum(t.CalculatePagesNum()); 		//设置所需要占用页面数目
					str=bf.readLine();		//额外读取一行
					line_count++;
					break;
				}
			}
			fr.close();
			bf.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public ArrayList<JCB> GetAllJCB()
	{
		return all_jcb;
	}
	
	public void NextJob()
	{
		//已经读取完一个作业，进入到下一个作业
		this.next_to_pcb++;
	}
	
	public int GetNextJobNum()
	{
		//获取下一个将要被创建的JCB的序号
		return this.next_to_pcb;
	}
	
	public boolean IsAllJobToProcess()
	{
		//检测是否还有作业没有变成进程
		return job_list.isEmpty();
	}
	
	public void RefreshJobList()
	{
		//刷新作业后备队列
		ArrayList<JCB> all=GetAllJCB();		//获取所有的JCB数组
		this.job_list.clear();			//清空后备队列
		int count=0;
		for(int i=GetNextJobNum();i<all.size();i++)
		{
			this.job_list.add(count++, all.get(i));
		}
	}
	
	public boolean IsJobListEmpty()
	{
		//查看作业后备队列是否为空
		return this.job_list.isEmpty();
	}
	
	public JCB CreateRandomJCB()
	{
		//待写
		//系统自动随机创建一个JCB
		return null;
	}
}

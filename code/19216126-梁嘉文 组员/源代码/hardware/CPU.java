package hardware;
import java.io.*;
import java.util.*;
//import os.PCB;
import os.kernel;

public class CPU
{
	public static CPU cpu=new CPU();
	
	public Timer ti;
	public MMU mm;
	public static Random random=new Random();		//CPU中的随机数生成器
	
	private int PC=0;		//地址寄存器PC
	private byte PSW=kernel.PSW_KERNEL_STATE;	//程序状态寄存器
	private int IR=0;	//指令寄存器
	private int CR3=0;	//页基址寄存器
	//public PCB current_pcb=0;		//当前执行的pcb
	
	public CPU()
	{
		ti=new Timer();
		mm=new MMU();
		InitCPU();
	}
	
	public void InitCPU()
	{
		//从文件读取CPU的信息，完成从文件到类的映射
		String str="";
		try {
			FileReader fr = new FileReader(kernel.CPUFILE_PATHNAME);
			BufferedReader bf = new BufferedReader(fr);
			str=bf.readLine();		//读取第一行
			str=bf.readLine();		//读取第二行
			String []sp1=str.split("=");	//分割，取得PC
			this.SetPC(Integer.parseInt(sp1[1]));
			str=bf.readLine();		//读取第三行
			String []sp2=str.split("=");	//分割，取得PSW
			this.SetPSW((byte) Integer.parseInt(sp2[1]));
			str=bf.readLine();		//读取第四行
			String []sp3=str.split("=");	//分割，取得IR
			this.SetIR((short) Integer.parseInt(sp3[1]));
			str=bf.readLine();		//读取第五行
			String []sp4=str.split("=");	//分割，取得CR3
			this.SetCR3(Integer.parseInt(sp4[1]));
			str=bf.readLine();		//读取第六行
			String []sp5=str.split("=");	//分割，取得SYSTEM_TIME
			kernel.SYSTEM_TIME=Integer.parseInt(sp5[1]);
			fr.close(); 		//关闭文件读入流
			bf.close(); 		//关闭缓冲输入流
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	/*public void Recovery(PCB pcb)
	{
		//恢复现场
		this.current_pcb=pcb;
		this.PC=current_pcb.GetCurrentInstructionNo()*kernel.SINGLE_INSTRUCTION_SIZE;
		this.PSW=current_pcb.GetPSW();
		if(current_pcb.getInstructions().size()<pcb.GetCurrentInstructionNo())
			this.IR=current_pcb.getInstructions().get(pcb.GetCurrentInstructionNo());
		this.CR3=CPU.cpu.mm.PageToRealAddress(current_pcb.page_table[current_pcb.GetCurrentInstructionNo()/kernel.SINGLE_INSTRUCTION_SIZE][1]);
	}*/
	
	public void PCSelfAdd()
	{
		//PC指针的自增
		this.PC+=kernel.SINGLE_INSTRUCTION_SIZE;
	}
	
	public void ClearPC()
	{
		//PC指针内容的清空
		this.PC=0;
	}
	
	public void SetPC(int pc)
	{
		//设置PC指针
		this.PC=pc;
	}
	
	public int GetPC()
	{
		//获得PC指针
		return this.PC;
	}
	
	public void SetPSW(byte PSW)
	{
		//设置PSW
		this.PSW=PSW;
	}
	
	public byte GetPSW()
	{
		//获取PSW
		return this.PSW;
	}
	
	public void SetIR(short instruction)
	{
		//设置指令
		this.IR=instruction;
	}
	
	public void ClearIR()
	{
		//清空指令
		this.IR=-1;
	}
	
	public int GetIR()
	{
		//获取IR指令
		return this.IR;
	}
	
	public void SetCR3(int address)
	{
		//设置CR3基址寄存器
		this.CR3=address;
	}
	
	public void ClearCR3()
	{
		//清空CR3基址寄存器
		this.CR3=0;
	}
	
	public int GetCR3()
	{
		//获取CR3
		return this.CR3;
	}
	
	/*public void SerCurrentPCB(PCB pcb)
	{
		//设置当前执行的PCB
		this.current_pcb=pcb;
	}
	
	public void ClearCurrentPCB()
	{
		//清空当前的PCB指针
		this.current_pcb=null;
	}*/
}

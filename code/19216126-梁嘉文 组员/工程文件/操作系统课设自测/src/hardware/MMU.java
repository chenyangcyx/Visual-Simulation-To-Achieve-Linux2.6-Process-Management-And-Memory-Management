package hardware;
//import os.PCB;
import os.kernel;

public class MMU
{
	private int[][] TLB=new int[kernel.TLB_LENGTH][2];	//TLB快表
	
	MMU()
	{
		ClearTLB();		//初始化TLB表
	}
	
	public void ClearTLB()
	{
		//清空快表
		for(int i=0;i<kernel.TLB_LENGTH;i++)
		{
			TLB[i][0]=-1;
			TLB[i][1]=-1;
		}
	}
	
	public int CheckTLBVirtualPageNo(short line)
	{
		//在TLB中检测第line行的虚拟页号
		return this.TLB[line][0];
	}
	
	public int CheckTLBRealPageNo(short line)
	{
		//在TLB中检测第line行中的实际页框号
		return this.TLB[line][1];
	}
	
	public int FindRealPageNo(short virtual_page_no)
	{
		//在TLB中查询对应的虚拟页号对应的实际页框号
		for(int i=0;i<kernel.TLB_LENGTH;i++)
		{
			if(this.TLB[i][0]==virtual_page_no)
				return CheckTLBRealPageNo((short) i);
		}
		return -1;
	}
	
	public void AddTLB(short virtual_page_no,short real_page_no)
	{
		//添加TLB数据
		short location=0;
		while(this.TLB[location][0]!=-1)
		{
			location=(short) ((location+1)%kernel.TLB_LENGTH);
		}
		this.TLB[location][0]=virtual_page_no;
		this.TLB[location][1]=real_page_no;
	}
	
	public void EditTLBData(short line,short data_1,short data_2)
	{
		//修改TLB快表的值
		this.TLB[line][0]=data_1;
		this.TLB[line][1]=data_2;
	}
	
	public short GetVirtualAddress_VirtualPage(short virtual_address)
	{
		//获取虚拟地址的页号
		return (short) ((virtual_address>>8)&0xFF);	
	}
	
	public short GetVirtualAddress_Offset(short virtual_address)
	{
		//获取虚拟地址的偏移，双字节数据的位移
		return (short) (virtual_address&0xFF);
	}
	
	public int PageToRealAddress(short num)
	{
		//将传入的页/块号num转换成为该页/块的基地址
		//该功能对内存与磁盘地址同样有效
		if(num<kernel.MEMORY_SIZE/kernel.SINGLE_PAGE_SIZE)
			return num*kernel.SINGLE_PAGE_SIZE;
		else
		{
			num=(short) (num-(kernel.MEMORY_SIZE/kernel.SINGLE_PAGE_SIZE));
			//cylinder磁道、sector扇区、offset偏移
			int cylinder=num/kernel.HARDDISK_SECTOR_NUM;	//计算磁道
			int sector=num%kernel.HARDDISK_SECTOR_NUM;		//计算扇区
			int offset=0;									//计算偏移
			return HardDisk.harddisk.CreateAddress(cylinder, sector, offset);
		}
	}
	
	/*public short VirtualAddressToRealAddress(PCB pcb,short virtual_address)
	{
		//将虚拟地址转换为实地址
		//注意，此时必须要保证所需要的页在内存中！！！！
		short virtual_page_no=GetVirtualAddress_VirtualPage(virtual_address);
		short virtual_offset=GetVirtualAddress_Offset(virtual_address);
		int real_page_no=FindRealPageNo(virtual_page_no);	//在TLB中查找对应的页框号
		if(real_page_no==-1)		//快表中不存在该项目
		{
			//去内存中查询该页号对应的页框号，放入到TLB中
			AddTLB(virtual_page_no,pcb.CheckPageTable(virtual_page_no));
			//重新查询TLB
			real_page_no=FindRealPageNo(virtual_page_no);
		}
		//else 快表中存在该项目
		if(PageToRealAddress((short) real_page_no)+virtual_offset<0x4000)
			return (short) ((short) (PageToRealAddress((short) real_page_no)+virtual_offset)+0x4000);
		else
			return (short) (PageToRealAddress((short) real_page_no)+virtual_offset);
	}*/
}

package os;

import hardware.CPU;
import hardware.HardDisk;
import hardware.Memory;

public class Page
{
	private int page_num;	//页号
	private short[] data=new short[kernel.SINGLE_PAGE_SIZE/2];	//每页大小=512B=256个short类型
	
	public Page(short num)
	{
		SetPageAllData(num);
	}
	
	public void SetPageNum(int num)
	{
		this.page_num=num;
	}
	
	public int GetPageNum()
	{
		return this.page_num;
	}
	
	public void SetPageAllData(short num)
	{
		SetPageNum(num);		//设置页号
		//初始化页面的数据
		if(page_num<kernel.MEMORY_SIZE/kernel.SINGLE_PAGE_SIZE)
		{
			//该页在内存中
			for(int i=0;i<kernel.SINGLE_PAGE_SIZE/2;i++)
			{
				data[i]=Memory.memory.GetData((short) (CPU.cpu.mm.PageToRealAddress((short) this.page_num)+i*2));
			}
		}
		else
		{
			//该页在外存中
			for(int i=0;i<kernel.SINGLE_PAGE_SIZE/2;i++)
			{
				data[i]=HardDisk.harddisk.GetData(CPU.cpu.mm.PageToRealAddress((short) this.page_num)+i*2);
			}
		}
	}
	
	public void SetPageData(short add,short data)
	{
		this.data[add/2]=data;
		//在写入页面数据时，同时修改对应的硬件中的数据
		if(page_num<kernel.MEMORY_SIZE/kernel.SINGLE_PAGE_SIZE)
		{
			//该页在内存中
			Memory.memory.WriteData((short) (CPU.cpu.mm.PageToRealAddress((short) this.page_num)+add),data);
		}
		else
		{
			//该页在外存中
			HardDisk.harddisk.WriteData(CPU.cpu.mm.PageToRealAddress((short) this.page_num)+add,data);
		}
	}
	
	public short GetPageData(short add)
	{
		//查询add为字节地址的数据，返回双字
		return this.data[add/2];
	}
	
	public void ClearPageData()
	{
		//清空该页面的所有数据，重置为0
		for(short i=0;i<kernel.SINGLE_PAGE_SIZE;i+=2)
			this.SetPageData(i, (short) 0);
	}
	
	public void CopyPageData(Page pa)
	{
		//从其他页中复制内容到该页，但是不复制页号
		for(short i=0;i<kernel.SINGLE_PAGE_SIZE;i+=2)
			this.SetPageData(i, pa.GetPageData(i));
	}
}

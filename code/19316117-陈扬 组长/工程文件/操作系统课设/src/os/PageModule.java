package os;
import java.util.*;

import hardware.CPU;
import hardware.HardDisk;
import hardware.Memory;

public class PageModule
{
	public static PageModule page_module=new PageModule();
	
	/*一些常量*/
	//用户区起始页号
	private final int userspace_page_location_start=kernel.MEMORY_KERNEL_SPACE_SIZE/kernel.SINGLE_PAGE_SIZE;
	//用户区结束页号
	private final int userspace_page_location_end=kernel.MEMORY_SIZE/kernel.SINGLE_PAGE_SIZE;
	//交换区起始页号
	private final int swaparea_page_location_start=kernel.MEMORY_SIZE/kernel.SINGLE_PAGE_SIZE;
	//交换区结束页号
	private final int swaparea_page_location_end=(kernel.MEMORY_SIZE+kernel.HARDDISK_VIRTUAL_MEMORY_SIZE)/kernel.SINGLE_PAGE_SIZE;
	/*一些常量*/
	
	//全部页面的使用情况
	private boolean []if_page_usage=new boolean[(kernel.MEMORY_SIZE+kernel.HARDDISK_VIRTUAL_MEMORY_SIZE)/kernel.SINGLE_PAGE_SIZE];
	//伙伴算法
	private int []bitmap=new int[6];	//每位对应1、2、4、8、16、32个页框，正好对应32位
	@SuppressWarnings("unchecked")
	private ArrayList<Short>[] free_area=new ArrayList[6];	//空闲链表
	
	public LinkedList<Integer> lru=new LinkedList<Integer>();
	
	PageModule()
	{
		InitSwapAreaUsage();	//初始化交换区页面被占用状态
		for(int i=0;i<6;i++)	//实例化伙伴算法的空闲链表
			this.free_area[i]=new ArrayList<Short>();
		InitFreeAreaList();		//初始化空闲链表
		RefreshBitmap();		//刷新伙伴算法的Bitmap
	}
	
	private void InitSwapAreaUsage()
	{
		//初始化交换区页的使用情况
		for(int i=this.swaparea_page_location_start;i<this.swaparea_page_location_end;i++)
			this.if_page_usage[i]=false;
	}
	
	private void InitFreeAreaList()
	{
		//初始化空闲链表
		for(int i=0;i<6;i++)
		{
			for(int j=0;j<(kernel.MEMORY_USER_SPACE_SIZE/kernel.SINGLE_PAGE_SIZE)/((int)Math.pow(2, i));j++)
			{
				free_area[i].add(j, (short) 0);	//未分配
			}
		}
	}
	
	public void RefreshBitmap()
	{
		//刷新bitmap
		for(int i=0;i<6;i++)
		{
			for(int j=0;j<(kernel.MEMORY_USER_SPACE_SIZE/kernel.SINGLE_PAGE_SIZE)/((int)Math.pow(2, i));j++)
			{
				this.bitmap[i]=SetOneBit(this.bitmap[i],j,this.free_area[i].get(j)==1?1:0);
			}
		}
	}
	
	private int SetOneBit(int num,int loca,int bit)
	{
		if(bit==1)
		{
			num|=(1<<loca);
		}
		else if(bit==0)
		{
			num&=~(1<<loca);
		}
		return num;
	}
	
	private int GetOneBit(int num,int loca)
	{
		return (num>>loca)&0x00000001;
	}
	
	public int GetFreePageNumInMemory()
	{
		//返回当前物理内存中可用的页框数
		RefreshBitmap();
		int count=0;
		for(int i=0;i<(kernel.MEMORY_USER_SPACE_SIZE/kernel.SINGLE_PAGE_SIZE);i++)
		{
			if(GetOneBit(this.bitmap[0],i)==0)
				count++;
		}
		return count;
	}
	
	public int GetFreePageNumInDisk()
	{
		//返回当前虚存中可用的页框数
		int count=0;
		for(int i=this.swaparea_page_location_start;i<this.swaparea_page_location_end;i++)
		{
			if(this.if_page_usage[i]==false)
				count++;
		}
		return count;
	}
	
	public Page GetPage(short num)
	{
		//获得某一个页面
		return new Page(num);
	}
	
	public boolean IfCouldApplyPageInDisk(short num)
	{
		//检测在虚存中是否可以申请num个页面
		if(this.GetFreePageNumInDisk()>=num)
			return true;
		else
			return false;
	}
	
	public int CalculateCloest2Num(short num)
	{
		//计算出与该数字最接近的2的次幂数的幂
		for(int i=0;i<=4;i++)
		{
			if(num==(short)Math.pow(2, i))
				return i;
			if(num>(short)Math.pow(2, i)&&num<(short)Math.pow(2, i+1))
				return i+1;
		}
		return -1;
	}
	
	private void SetBlockState(int list,int no,int set_num,int state)
	{
		//在伙伴算法的链表中，在第list级别的第no块设置连续的set_num块为state状态
		if(list>=0)
		{
			for(int i=no;i<no+set_num;i++)
			{
				free_area[list].set(i, (short) state);
			}
			SetBlockState(list-1,no*2,set_num*2,state);		//递归调用自身，修改该节点往下的所有节点
		}
		else
			return;
	}
	
	private void RefreshBlockList()
	{
		//从底往上刷新块链表
		for(int i=0;i<=4;i++)
		{
			for(int j=0;j<(kernel.MEMORY_USER_SPACE_SIZE/kernel.SINGLE_PAGE_SIZE)/((int)Math.pow(2, i));j+=2)
			{
				//对于占用的处理
				if(free_area[i].get(j)==1||free_area[i].get(j+1)==1)
					free_area[i+1].set(j/2, (short) 1);
				//对于空闲的处理
				if(free_area[i].get(j)==0&&free_area[i].get(j+1)==0)
					free_area[i+1].set(j/2, (short) 0);
			}
		}
		RefreshBitmap();	//刷新bitmap
	}
	
	public String ApplyPageInMemory(short num)
	{
		//在内存中，向伙伴算法申请num个页面
		int pow=CalculateCloest2Num(num);	//求出该页面所需要申请的块所在的链表级别
		String apply_str="null";	//默认为该页面无法找到
		for(int i=0;i<(kernel.MEMORY_USER_SPACE_SIZE/kernel.SINGLE_PAGE_SIZE)/(int)Math.pow(2, pow);i++)
		{
			if(free_area[pow].get(i)==0)	//找到未分配的块
			{
				apply_str=""+pow+":"+i;		//写入字符串，表示分配方式："链表:块号"
				SetBlockState(pow,i,1,1);	//设置块链表的该位为1
				RefreshBlockList();			//刷新块链表
				break;
			}
		}
		return apply_str;
	}
	
	public String ApplyPageInDisk(short page_num)
	{
		//在虚存中申请page_num个页面，返回一个String类型的值
		//String格式的数据说明：从左到右编号为0-191，共192位，每一位的值为0/1，1代表该页面分配给该进程使用。在写入程序区时，必须按照分配的页面顺序从小到大写入
		//同时，在记录数组中记录这些申请的页框，将他们设置为已用状态
		if(page_num>GetFreePageNumInDisk())
			return "null";
		int count=0;
		String str="";
		for(int i=0;i<this.swaparea_page_location_end;i++)
		{
			if(i>=this.swaparea_page_location_start)
			{
				if(this.if_page_usage[i]==false)	//找到空闲的页面
				{
					str+="1";
					count++;
					this.if_page_usage[i]=true;
				}
				else
					str+="0";
			}
			else
				str+="0";
			if(count==page_num)
				break;
		}
		return str;
	}
	
	public void FreePageInMemory(short page_num)
	{
		//在内存中，利用伙伴算法释放某一页
		//将这一页内容清空
		Page pa=new Page(page_num);
		for(short i=0;i<kernel.SINGLE_PAGE_SIZE;i+=2)
			pa.SetPageData(i, (short) 0);
		//在伙伴算法块链表中，将这一页所在的块清空
		SetBlockState(0,page_num-32,1,0);	//设置块链表的该位为0
		RefreshBlockList();					//刷新块链表
	}
	
	public void FreePageInDisk(short page_num)
	{
		//在外存中，释放某一页
		//将这一页内容清空
		Page pa=new Page(page_num);
		for(short i=0;i<kernel.SINGLE_PAGE_SIZE;i+=2)
			pa.SetPageData(i, (short) 0);
		//将这一页设置为未占用状态
		this.if_page_usage[page_num]=false;
	}
	
	public void RecyclePage(short page_num)
	{
		//回收页面，参数num为页面号（num从0开始编号）
		//将该页的内容全部清空，并在记录中使得该页表示为未被占用
		if(page_num>=this.userspace_page_location_start&&page_num<this.userspace_page_location_end)
			FreePageInMemory(page_num);	//释放用户空间中的页
		if(page_num>=this.swaparea_page_location_start&&page_num<this.swaparea_page_location_end)
			FreePageInDisk(page_num);	//释放交换区中的页
	}
	
	public void MoveToMemory(short memory_page_num,short swap_page_num)
	{
		//将指定的虚存页移动到指定的内存页中
		/*
		Page mem_page=new Page(memory_page_num);
		Page swap_page=new Page(swap_page_num);
		for(short i=0;i<kernel.SINGLE_PAGE_SIZE;i+=2)
		{
			mem_page.CopyPageData(swap_page);	//内容转移
			swap_page.ClearPageData();			//内容清空
		}
		*/
		for(int i=0;i<kernel.SINGLE_PAGE_SIZE/2;i++)
		{
			//数据的转移
			Memory.memory.WriteData((short) (CPU.cpu.mm.PageToRealAddress(memory_page_num)+2*i), 
					HardDisk.harddisk.GetData(CPU.cpu.mm.PageToRealAddress((short) ((swap_page_num)+i*2))));
			//内容清空
			HardDisk.harddisk.WriteData(CPU.cpu.mm.PageToRealAddress(swap_page_num)+i*2, (short)0);
		}
	}
	
	public void MoveToDisk(short memory_page_num,short swap_page_num)
	{
		//将指定的内存页移动到指定的虚存页中
		Page mem_page=new Page(memory_page_num);
		Page swap_page=new Page(swap_page_num);
		for(short i=0;i<kernel.SINGLE_PAGE_SIZE;i+=2)
		{
			swap_page.CopyPageData(mem_page);	//内容转移
			mem_page.ClearPageData();			//内容清空
		}
	}
	
	public void ExchangePage(short memory_page_num,short swap_page_num)
	{
		//交换两个页面的内容
		Page p1=new Page(memory_page_num);
		Page p2=new Page(swap_page_num);
		short temp=0;
		for(short i=0;i<kernel.SINGLE_PAGE_SIZE;i+=2)
		{
			temp=p1.GetPageData(i);
			p1.SetPageData(i, p2.GetPageData(i));
			p2.SetPageData(i, temp);
		}
	}
	
	public void CopyPage(short src_page_num,short des_page_num)
	{
		Page src=new Page(src_page_num);	//被复制的页面
		Page des=new Page(des_page_num);	//要复制到的页面
		des.CopyPageData(src);
	}
	
	public short GetOneFreePageInMemory()
	{
		//在物理内存中找到一个空闲的页面，并返回该页面序号
		String apply_str=ApplyPageInMemory((short)1);
		if(apply_str.equals("null")==true)
			return -1;
		String []split=apply_str.split(":");
		return (short) (Integer.valueOf(split[1]).shortValue()+32);
	}
	
	public short GetOneFreePageInDisk()
	{
		//在虚存中找到一个空闲的页面，并返回该页面序号
		String apply_str=ApplyPageInDisk((short) 1);
		for(int i=0;i<this.swaparea_page_location_end;i++)
			if(apply_str.charAt(i)=='1')
				return (short) i;
		return -1;
	}
	
	public void LRUVisitOnePage(int page_num)
	{
		//LRU访问某一页
		lru.remove(Integer.valueOf(page_num));		//将该页号从原来的里面删除
		lru.addFirst(Integer.valueOf(page_num)); 	//将访问的该页置顶
	}
	
	public short LRUGetLastPageNum()
	{
		//获得应该调出的页面号
		return lru.getLast().shortValue();		//获得链表的最后一项
	}
	
	public boolean isBlockUsing(int i,int j)
	{
		if(GetOneBit(bitmap[Math.abs(5-i)],j)==1)
		{
			return true;
		}
		return false;
	}
	
	public boolean isPageUsing(int i)
	{
		if(if_page_usage[i])
		{
			return true;
		}
		return false;
	}

}

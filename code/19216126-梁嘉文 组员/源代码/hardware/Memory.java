package hardware;
import java.io.*;
import os.kernel;

public class Memory
{
	public static Memory memory=new Memory();
	
	private byte []data=new byte[32*1024];		//32KB=32768B
	
	public Memory()
	{
		InitMemory();
	}
	
	public void InitMemory()
	{
		//从文件中读取内存文件的内容，放入data数组，以字节为单位
		try
		{
			FileInputStream reader=new FileInputStream(kernel.MEMORYFILE_PATHNAME);
			reader.read(data);
			reader.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//将byte组装成short类型数据
	private short ByteToShort(short address)
	{
		int temp=0;
		if(address%2==0)
		{
			temp=((short)this.data[address]<<8)&0xFF00;
			temp|=this.data[address+1]&0x00FF;
		}
		else
		{
			temp=(this.data[address-1]<<8)&0xFF00;
			temp|=this.data[address]&0x00FF;
		}
		return (short) temp;
	}
	
	public void WriteData(short address,short data)
	{
		AddressLine.address_line.WriteAddress(address); 			//向地址线中写入地址信息
		DataLine.data_line.WriteData(data); 					//向数据线中写入数据信息
		short address_temp=AddressLine.address_line.GetAddress();
		short data_temp=DataLine.data_line.GetData();
		
		if(address_temp%2==0)
		{
			this.data[address_temp]=(byte) (data_temp>>8);		//取高8位
			this.data[address_temp+1]=(byte) (data_temp);		//取低8位
		}
		else
		{
			this.data[address_temp-1]=(byte) (data_temp>>8);		//取高8位
			this.data[address_temp]=(byte) (data_temp);				//取低8位
		}
		//将数据线中的信息写入到地址线指示的内存地址
	}
	
	public short GetData(short address)
	{
		AddressLine.address_line.WriteAddress(address);			//向地址线中写入地址信息
		DataLine.data_line.WriteData(ByteToShort(AddressLine.address_line.GetAddress()));
		return DataLine.data_line.GetData();
		//根据地址线中的地址信息取出内存中相应的数据
	}
}

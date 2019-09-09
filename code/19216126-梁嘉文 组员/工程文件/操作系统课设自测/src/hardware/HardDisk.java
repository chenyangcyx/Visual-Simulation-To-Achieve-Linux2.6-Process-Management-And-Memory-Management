package hardware;
import java.io.*;
import os.kernel;

public class HardDisk
{
	public static HardDisk harddisk=new HardDisk();
	
	private byte [][][]data=new byte[32][64][512];
	
	public HardDisk()
	{
		InitHardDisk();
	}
	
	public void InitHardDisk()
	{
		//从文件中读取硬盘文件的内容，放入data数组，以字节为单位
		try
		{
			FileInputStream reader=new FileInputStream(kernel.HARDDISKFILE_PATHNAME);
			for(int i=0;i<kernel.HARDDISK_CYLINDER_NUM;i++)
			{
				for(int j=0;j<kernel.HARDDISK_SECTOR_NUM;j++)
					reader.read(data[i][j]);
			}
			reader.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//根据磁道、扇区、偏移计算地址
	public int CreateAddress(int cylinder,int sector,int offset)
	{
		int address=0;
		address=(address|offset)&0x001FF;
		address=(address|(sector<<9))&0x07FFF;
		address=(address|(cylinder<<15))&0xFFFFF;
		return address;
	}
	
	//将byte组装成short类型数据
	private short ByteToShort(short cylinder,short sector,short offset)
	{
		//cylinder磁道、sector扇区、offset偏移
		//地址结构：偏移——扇区——磁道
		//占用位数：偏移0-8   扇区9-14  磁道15-19
		short temp=0;
		if(offset%2==0)
		{
			temp=(short)((short)(this.data[cylinder][sector][offset])<<8);
			temp|=this.data[cylinder][sector][offset+1]&0x00FF;
		}
		else
		{
			temp=(short)((short)(this.data[cylinder][sector][offset-1])<<8);
			temp|=this.data[cylinder][sector][offset]&0x00FF;
		}
		return temp;
	}
	
	public void WriteData(int address,short data)
	{
		short address_0_15=0;
		short address_16_31=0;
		AddressLine.address_line.WriteAddress((short) (address&0x0000FFFF));
		address_0_15=AddressLine.address_line.GetAddress();
		AddressLine.address_line.WriteAddress((short) ((address>>16)&0x0000FFFF));
		address_16_31=AddressLine.address_line.GetAddress();
		//cylinder磁道、sector扇区、offset偏移
		//地址结构：偏移——扇区——磁道
		//占用位数：偏移0-8   扇区9-14  磁道15-19
		short offset=(short) (address_0_15&0x01FF);
		short sector=(short) ((address_0_15>>9)&0x03F);
		short cylinder=(short) ((short) ((address_0_15&0x08000)>>11)|(short) (address_16_31&0x0F));
		
		if(offset%2==0)
		{
			DataLine.data_line.WriteData(data);
			short data_temp=DataLine.data_line.GetData();
			this.data[cylinder][sector][offset]=(byte) ((data_temp>>8)&0x00FF);
			this.data[cylinder][sector][offset+1]=(byte) (data_temp&0x00FF);
		}
		else
		{
			DataLine.data_line.WriteData(data);
			short data_temp=DataLine.data_line.GetData();
			this.data[cylinder][sector][offset-1]=(byte) ((data_temp>>8)&0x00FF);
			this.data[cylinder][sector][offset]=(byte) (data_temp&0x00FF);
		}
	}
	
	public short GetData(int address)
	{
		short address_0_15=0;
		short address_16_31=0;
		AddressLine.address_line.WriteAddress((short) (address&0x0000FFFF));
		address_0_15=AddressLine.address_line.GetAddress();
		AddressLine.address_line.WriteAddress((short) ((address>>16)&0x0000FFFF));
		address_16_31=AddressLine.address_line.GetAddress();
		//cylinder磁道、sector扇区、offset偏移
		//地址结构：偏移——扇区——磁道
		//占用位数：偏移0-8   扇区9-14  磁道15-19
		short offset=(short) (address_0_15&0x01FF);
		short sector=(short) ((address_0_15>>9)&0x03F);
		short cylinder=(short) ((short) ((address_0_15&0x08000)>>11)|(short) (address_16_31&0x0F));
		
		DataLine.data_line.WriteData(ByteToShort(cylinder,sector,offset));
		return DataLine.data_line.GetData();
	}
}

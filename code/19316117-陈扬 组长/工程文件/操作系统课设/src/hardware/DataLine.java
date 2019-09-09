package hardware;
public class DataLine
{
	//数据线的静态变量
	public static DataLine data_line=new DataLine();
	
	//数据线存储单元
	private short dataline_data=0;
	
	//存入数据信息
	public void WriteData(short data)
	{
		this.dataline_data=data;
	}
	
	//取出数据信息
	public short GetData()
	{
		short temp=this.dataline_data;
		this.dataline_data=0;
		return temp;
	}
}

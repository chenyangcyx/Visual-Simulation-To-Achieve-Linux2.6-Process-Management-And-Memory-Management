package hardware;
public class AddressLine
{
	//地址线的静态变量
	public static AddressLine address_line=new AddressLine();
	
	//地址线存储单元
	private short addressline_data=0;
	
	//存入地址信息
	public void WriteAddress(short data)
	{
		this.addressline_data=data;
	}
	
	//取出地址信息
	public short GetAddress()
	{
		short temp=this.addressline_data;
		this.addressline_data=0;
		return temp;
	}
}

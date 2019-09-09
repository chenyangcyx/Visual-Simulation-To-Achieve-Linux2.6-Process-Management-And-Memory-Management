package hardware;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import os.kernel;

public class Timer extends Thread
{
	private boolean if_interrupt=false;		//是否发生中断的标志位
	private String current_time="";			//存储当前时间的字符串
	SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss");
	Calendar calendar;
    Date date;
    public int time=-10;	//计时器的时间
	
	public void run()
	{
		while(true)
		{
			try
			{
				sleep(kernel.INTERRUPTION_INTERVAL);		//睡眠一定时间
				if_interrupt=true;
				time+=kernel.INTERRUPTION_INTERVAL;			//CPU内时间自增
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public boolean GetIfInterrupt()
	{
		return if_interrupt;
	}
	
	public void ResetIfInterrupt()
	{
		if_interrupt=false;
	}
	
	public String GetCurrentTime()
	{
		calendar = Calendar.getInstance();
	    date = calendar.getTime();
	    current_time = sdf.format(date);
	    return current_time;
	}
	
	public int GetSystemTime()
	{
		return kernel.SYSTEM_TIME;
	}
}

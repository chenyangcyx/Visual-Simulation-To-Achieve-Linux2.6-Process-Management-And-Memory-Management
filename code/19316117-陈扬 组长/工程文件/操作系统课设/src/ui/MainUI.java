package ui;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import hardware.*;
import os.*;

public class MainUI
{
	public static MainUI main_ui=new MainUI();
	
	public String run_info="";
	private JPanel job_queue=new JPanel();		//后备队列面板
	private JPanel running_queue=new JPanel();	//运行队列面板
	private JPanel ready_queue=new JPanel();	//就绪队列面板
	private JPanel wait_queue=new JPanel();		//等待队列面板
	private JPanel suspend_queue=new JPanel();	//挂起队列面板
	private JPanel state_panel=new JPanel();	//状态信息面板
	private JPanel run_info_panel=new JPanel();	//总信息面板
	
	private JTextArea job_info=new JTextArea();			//后备队列信息
	private JTextArea running_info=new JTextArea();		//运行队列信息
	private JTextArea ready_info=new JTextArea();		//就绪队列信息
	private JTextArea wait_info=new JTextArea();		//等待队列信息
	private JTextArea suspend_info=new JTextArea();		//挂起队列信息
	private JTextArea state_info_text=new JTextArea();	//状态信息显示
	JTextArea info_show=new JTextArea();				//总信息显示
	
	private JScrollPane job_queue_scro=new JScrollPane(job_info);	//后备队列信息滚动
	private JScrollPane running_queue_scro=new JScrollPane(running_info);	//后备队列信息滚动
	private JScrollPane ready_queue_scro=new JScrollPane(ready_info);	//后备队列信息滚动
	private JScrollPane wait_queue_scro=new JScrollPane(wait_info);	//后备队列信息滚动
	private JScrollPane suspend_queue_scro=new JScrollPane(suspend_info);	//后备队列信息滚动
	private JScrollPane state_info_scro=new JScrollPane(state_info_text);		//状态信息滚动
	private JScrollPane scro=new JScrollPane(info_show);				//总信息信息滚动
	
	private JButton load_job=new JButton("载入作业");				//按钮-载入作业
	private JButton create_job=new JButton("创建作业");				//按钮-创建作业
	private JButton save_run_info=new JButton("保存执行结果");		//按钮-保存执行结果
	private JButton system_start=new JButton("启动");				//按钮-启动
	private JButton clear_screen=new JButton("清屏");				//按钮-结束
	private JButton system_exit=new JButton("退出");				//按钮-退出
	
	private JButton mem_ui=new JButton("内存界面");					//按钮-调出内存界面
	private JButton hd_ui=new JButton("磁盘界面");					//按钮-调出磁盘界面
	private JButton page_ui=new JButton("页面监测");				//按钮-调出页面检测界面
	private JButton cpu_ui=new JButton("cpu监测");				//按钮-调出cpu界面
	
	File file=null;
	FileDialog fileDialog=null;
	
	JFrame fra=new JFrame("主界面");
	
	MainUI()
	{
		/*运行队列面板*/
		running_queue.setBorder(BorderFactory.createTitledBorder("运行队列"));			//给面板加框
		running_queue.add(running_queue_scro);	//添加显示框
		running_queue.setLayout(null);
		running_queue_scro.setBounds(5, 20, 490, 75);
		running_info.setEditable(false); 	//设置不可编辑
		/*运行队列面板*/
		
		/*就绪队列面板*/
		ready_queue.setBorder(BorderFactory.createTitledBorder("就绪队列"));			//给面板加框
		ready_queue.add(ready_queue_scro);	//添加显示框
		ready_queue.setLayout(null);
		ready_queue_scro.setBounds(5, 20, 470, 75);
		ready_info.setEditable(false); 	//设置不可编辑
		/*就绪队列面板*/
		
		/*等待队列面板*/
		wait_queue.setBorder(BorderFactory.createTitledBorder("等待队列"));			//给面板加框
		wait_queue.add(wait_queue_scro);	//添加显示框
		wait_queue.setLayout(null);
		wait_queue_scro.setBounds(5, 20, 490, 75);
		wait_info.setEditable(false); 	//设置不可编辑
		/*等待队列面板*/
		
		/*挂起队列面板*/
		suspend_queue.setBorder(BorderFactory.createTitledBorder("挂起队列"));			//给面板加框
		suspend_queue.add(suspend_queue_scro);	//添加显示框
		suspend_queue.setLayout(null);
		suspend_queue_scro.setBounds(5, 20, 470, 75);
		suspend_info.setEditable(false); 	//设置不可编辑
		/*挂起队列面板*/
		
		/*后备队列面板*/
		job_queue.setBorder(BorderFactory.createTitledBorder("后备队列"));			//给面板加框
		job_queue.add(job_queue_scro);		//添加显示框
		job_queue.setLayout(null);
		job_queue_scro.setBounds(5, 20, 490, 75);
		job_info.setEditable(false); 		//设置不可编辑状态
		/*后备队列面板*/
		
		/*状态信息面板*/
		state_panel.setBorder(BorderFactory.createTitledBorder("状态信息"));		 //给面板加框
		state_panel.add(state_info_scro);
		state_panel.setLayout(null);
		state_info_scro.setBounds(5, 20, 470, 75);
		/*状态信息面板*/
		
		/*总信息面板*/
		run_info_panel.setBorder(BorderFactory.createTitledBorder("运行信息"));		//给面板加框
		run_info_panel.add(scro);
		run_info_panel.setLayout(null);
		scro.setBounds(20, 20, 950, 200);
		/*总信息面板*/
		
		/*框架设置*/
		fra.setLayout(null);
		fra.setResizable(false);
		fra.add(running_queue);		//运行队列
		running_queue.setBounds(0, 0, 500, 100);
		fra.add(ready_queue);		//就绪队列
		ready_queue.setBounds(500, 0, 480, 100);
		fra.add(wait_queue);		//等待队列
		wait_queue.setBounds(0, 120, 500, 100);
		fra.add(suspend_queue);		//挂起队列
		suspend_queue.setBounds(500, 120, 480, 100);
		fra.add(job_queue);			//后备队列
		job_queue.setBounds(0, 240, 500, 100);
		fra.add(state_panel);
		state_panel.setBounds(500, 240, 500, 100);
		fra.add(run_info_panel);		//总信息面板
		run_info_panel.setBounds(0, 350, 980, 225);
		fra.setSize(1000,700);
		/*框架设置*/
		
		/*按钮添加*/
		fra.add(load_job);					//按钮-载入作业
		fra.add(create_job);				//按钮-创建作业
		fra.add(save_run_info);				//按钮-保存程序运行结果
		fra.add(system_start);				//按钮-启动
		fra.add(clear_screen);				//按钮-结束
		fra.add(system_exit);				//按钮-退出

		fra.add(mem_ui);					//按钮-调出内存界面
		fra.add(hd_ui);						//按钮-调出磁盘界面
		fra.add(page_ui);					//按钮-调出页面检测界面
		fra.add(cpu_ui);					//按钮-调出cpu监测界面
		
		load_job.setBounds(40, 600, 100, 30);
		create_job.setBounds(140, 600, 100, 30);
		save_run_info.setBounds(240, 600, 140, 30);
		system_start.setBounds(380, 600, 100, 30);
		clear_screen.setBounds(480, 600, 100, 30);
		system_exit.setBounds(580, 600, 100, 30);
		
		mem_ui.setBounds(730, 580, 100, 30);
		hd_ui.setBounds(830, 580, 100, 30);
		page_ui.setBounds(730, 615, 100, 30);
		cpu_ui.setBounds(830, 615, 100, 30);
		/*按钮添加*/
		
		//添加按钮监听
		AddAcitonListener();
	}
	
	public void RefreshData()
	{
		//显示运行队列
		if(ProcessModule.process_module.running_queue.isEmpty())
			running_info.setText("空");
		for(int i=0;i<ProcessModule.process_module.running_queue.size();i++)
		{
			if(i==0)
				running_info.setText("进程"+ProcessModule.process_module.running_queue.get(i).GetPid());
			else
				running_info.append("<--进程"+ProcessModule.process_module.running_queue.get(i).GetPid());
			if((i+1)%5==0)
				running_info.append("\n");
		}
		
		//显示就绪队列
		if(ProcessModule.process_module.IsReadyQueueEmpty())
			ready_info.setText("active:\n\nexpired:\n");
		else
		{
			ready_info.setText("active:\n");
			int count1=0;
			for(int i=0;i<140;i++)
			{
				for(int j=0;j<ProcessModule.process_module.ready_queue[ProcessModule.process_module.GetActivePoint()][i].size();j++)
				{
					if(count1==0)
						ready_info.append("作业"+ProcessModule.process_module.ready_queue[ProcessModule.process_module.GetActivePoint()][i].get(j).GetPid());
					else
						ready_info.append("<--作业"+ProcessModule.process_module.ready_queue[ProcessModule.process_module.GetActivePoint()][i].get(j).GetPid());
					count1++;
				}
			}
			ready_info.append("\nexpired:\n");
			int count2=0;
			for(int i=0;i<140;i++)
			{
				for(int j=0;j<ProcessModule.process_module.ready_queue[ProcessModule.process_module.GetExpiredPoint()][i].size();j++)
				{
					if(count2==0)
					{
						ready_info.append("作业"+ProcessModule.process_module.ready_queue[ProcessModule.process_module.GetExpiredPoint()][i].get(j).GetPid());
					}
					else
						ready_info.append("<--作业"+ProcessModule.process_module.ready_queue[ProcessModule.process_module.GetExpiredPoint()][i].get(j).GetPid());
					count2++;
				}
			}
		}
		
		//显示等待队列
		if(ProcessModule.process_module.waiting_queue.isEmpty())
			wait_info.setText("空");
		for(int i=0;i<ProcessModule.process_module.waiting_queue.size();i++)
		{
			if(i==0)
				wait_info.setText("进程"+ProcessModule.process_module.waiting_queue.get(i).GetPid());
			else
				wait_info.append("<--进程"+ProcessModule.process_module.waiting_queue.get(i).GetPid());
			if((i+1)%5==0)
				wait_info.append("\n");
		}
		//显示挂起队列
		if(ProcessModule.process_module.suspend_queue.isEmpty())
			suspend_info.setText("空");
		for(int i=0;i<ProcessModule.process_module.suspend_queue.size();i++)
		{
			if(i==0)
				suspend_info.setText("进程"+ProcessModule.process_module.suspend_queue.get(i).GetPid());
			else
				suspend_info.append("<--进程"+ProcessModule.process_module.suspend_queue.get(i).GetPid());
			if((i+1)%5==0)
				suspend_info.append("\n");
		}
		//显示后备队列
		if(JobModule.job_module.job_list.size()==0)
			job_info.setText("空");
		for(int i=0;i<JobModule.job_module.job_list.size();i++)
		{
			if(i==0)
				job_info.setText("作业"+String.valueOf(JobModule.job_module.job_list.get(0).GetJobid()));
			else
				job_info.append("<--作业"+String.valueOf(JobModule.job_module.job_list.get(i).GetJobid()));
			if((i+1)%5==0)
				job_info.append("\n");
		}
		//显示状态信息
		if(CPU.cpu.current_pcb==null)
		{
			state_info_text.setText("空");
		}
		else
		{
			state_info_text.setText("当前进程："+CPU.cpu.current_pcb.GetPid());
			state_info_text.append("\n优先级："+CPU.cpu.current_pcb.GetPriority());
			state_info_text.append("\n时间片长度："+CPU.cpu.current_pcb.GetTimeslice());
			state_info_text.append("\n已经运行时间："+CPU.cpu.current_pcb.GetRuntime());
			state_info_text.append("\n当前运行到的指令序号："+CPU.cpu.current_pcb.GetCurrentInstructionNo());
			state_info_text.append("\n包含指令数目："+CPU.cpu.current_pcb.GetInstruction_num());
			state_info_text.append("\n占用页面数："+CPU.cpu.current_pcb.GetPagesNum());
		}
		//显示程序运行信息
		info_show.setText(run_info);
		//info_show.setCaretPosition(info_show.getDocument().getLength());	//设置滚动条始终在最后
	}
	
	public void SetVisible(boolean visible)
	{
		this.fra.setVisible(visible);
	}
	
	private void AddAcitonListener()
	{
		//载入作业
		load_job.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc=new JFileChooser();
				jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES );
				jfc.showDialog(new JLabel(), "选择");
				File file=jfc.getSelectedFile();
				if(file==null)
					return;
				JobModule.job_module.GetJCBFromFile(file);
				for(int i=0;i<JobModule.job_module.GetAllJCB().size();i++)
				{
					JobModule.job_module.SaveJobToHardDisk(JobModule.job_module.GetAllJCB().get(i));
				}
				JobModule.job_module.RefreshJobList();			//刷新后备队列
			}
		});
		
		//创建作业
		create_job.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CreateJobUI.create_job_ui.SetVisible(true);
			}
		});
		
		//保存程序运行结果
		save_run_info.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(file==null)
				{
					fileDialog=new FileDialog(fra,"保存",FileDialog.SAVE);
					fileDialog.setVisible(true);
					if ((fileDialog.getDirectory()!=null) && (fileDialog.getFile()!=null))
	                {
						//文件对象的赋值
	                    file= new File(fileDialog.getDirectory(),fileDialog.getFile());
	                    SaveFile(file);		//调用自定义的save方法
	                }
				}
				else
				{
					SaveFile(file);
				}
			}
		});
		
		//启动
		system_start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Control.con.SystemStart();
			}
		});
		
		//清屏
		clear_screen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				run_info="";
				Scheduling.sch.UIRefresh();
			}
		});
		
		//退出按钮
		system_exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		
		//内存界面
		mem_ui.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MemoryUI.mem_ui.SetVisable(true);
			}
		});
		
		//磁盘界面
		hd_ui.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HardDiskUI.hd_ui.SetVisable(true);
			}
		});
		
		//页面管理界面
		page_ui.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PageModuleUI.page_ui.SetVisable(true);
			}
		});
		
		//CPU界面
		cpu_ui.addActionListener(new ActionListener() { 
			@Override
			public void actionPerformed(ActionEvent e) {
				CPUInfoUI.cpu_info_ui.setVisible(true);
			}
		});
	}
	
	private void SaveFile(File file)
	{
		//保存文件
		try
		{
			FileWriter fw = new FileWriter(file);
			fw.write("----程序运行信息----\n");		//文件表头信息
			fw.write("保存时间："+CPU.cpu.ti.GetCurrentTime()+"      机器时间："+kernel.SYSTEM_TIME+"\n\n");	//写入时间
			fw.write(MainUI.main_ui.run_info);
			fw.close();
		}catch (IOException e){}
	}
}

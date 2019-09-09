package ui;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import os.*;

public class CreateJobUI
{
	public static CreateJobUI create_job_ui=new CreateJobUI();
	
	private JFrame fra=new JFrame("创建作业");
	
	private JLabel job_id_label=new JLabel("作业序号：");
	private JLabel job_priority_label=new JLabel("作业优先级：");
	private JLabel instruction_num_label=new JLabel("包含指令数目：");
	private JLabel instrcution_label=new JLabel("所有指令：");
	private JTextArea all_instruction=new JTextArea();
	private JScrollPane all_scro=new JScrollPane(all_instruction);
	private JTextField input_id=new JTextField();
	private JTextField input_priority=new JTextField();
	private JTextField input_instruction_num=new JTextField();
	
	private JButton save_job=new JButton("创建");
	private JButton random_create=new JButton("随机创建");
	private JButton close=new JButton("关闭");
	
	CreateJobUI()
	{
		fra.setLayout(null);
		fra.add(job_id_label);
		job_id_label.setBounds(20, 20, 100, 20);
		job_id_label.setFont(new Font("微软雅黑",Font.PLAIN,14));
		fra.add(job_priority_label);
		job_priority_label.setBounds(20, 60, 100, 20);
		job_priority_label.setFont(new Font("微软雅黑",Font.PLAIN,14));
		fra.add(instruction_num_label);
		instruction_num_label.setBounds(20, 100, 120, 20);
		instruction_num_label.setFont(new Font("微软雅黑",Font.PLAIN,14));
		fra.add(instrcution_label);
		instrcution_label.setBounds(20, 140, 100, 20);
		instrcution_label.setFont(new Font("微软雅黑",Font.PLAIN,14));
		fra.add(all_scro);
		all_scro.setBounds(20, 180, 300, 150);
		fra.add(input_id);
		input_id.setBounds(150, 20, 170, 20);
		fra.add(input_priority);
		input_priority.setBounds(150, 60, 170, 20);
		fra.add(input_instruction_num);
		input_instruction_num.setBounds(150, 100, 170, 20);
		
		fra.add(save_job);
		save_job.setFont(new Font("微软雅黑",Font.PLAIN,20));
		save_job.setBounds(20, 350, 100, 50);
		fra.add(random_create);
		random_create.setFont(new Font("微软雅黑",Font.PLAIN,15));
		random_create.setBounds(120, 350, 100, 50);
		fra.add(close);
		close.setFont(new Font("微软雅黑",Font.PLAIN,20));
		close.setBounds(220, 350, 100, 50);
		fra.setSize(360,460);
		//fra.setVisible(false);
		AddActionListener();
	}
	
	public void SetVisible(boolean visible)
	{
		if(visible==true)
		{
			input_id.setText("");
			input_priority.setText("");
			input_instruction_num.setText("");
		}
		this.fra.setVisible(visible);
	}
	
	private void AddActionListener()
	{
		//添加创建-按钮监听
		save_job.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JCB t=new JCB();
				t.SetJobid((short) Integer.parseInt(input_id.getText()));
				t.SetPriority((short) Integer.parseInt(input_priority.getText()));
				t.SetInstructionNum((short) Integer.parseInt(input_instruction_num.getText()));
				String []sp=all_instruction.getText().split(",");
				for(int i=0;i<sp.length;i++)
					t.GetAll_Instructions().add((short) Integer.parseInt(sp[i]));
				t.SetJobIntime(kernel.SYSTEM_TIME); 		//设置作业进入时间
				t.SetPagesNum(t.CalculatePagesNum()); 		//设置所需要占用页面数目
				JobModule.job_module.GetAllJCB().add(t);
				JobModule.job_module.SaveJobToHardDisk(t);
				JobModule.job_module.RefreshJobList();			//刷新后备队列
			}
		});
		
		//随机创建作业
		random_create.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int job_num=(int) (1+Math.random()*(20-1+1));		//随机创建1-20个JCB
				FileDialog fileDialog=null;
				fileDialog=new FileDialog(fra,"保存",FileDialog.SAVE);
				fileDialog.setVisible(true);
				File file=null;
				if ((fileDialog.getDirectory()!=null) && (fileDialog.getFile()!=null))
					//文件对象的赋值
					file= new File(fileDialog.getDirectory(),fileDialog.getFile());
				int job_id,priority,instruction_num;
				ArrayList<Integer> all_instructions=new ArrayList<Integer>();
				for(int i=0;i<job_num;i++)
				{
					job_id=i+1;			//生成JCB的ID
					priority=(int) (-20+Math.random()*(19-(-20)+1));	//生成作业的priority
					instruction_num=(int) (5+Math.random()*(200-5+1));	//生成作业的指令数目
					all_instructions.clear();
					for(int j=0;j<instruction_num;j++)
						all_instructions.add((int) (1+Math.random()*(59-1+1)));	//随机生成指令
					if(i==0)
						SaveFile(file,true,job_id,priority,instruction_num,all_instructions);
					else
						SaveFile(file,false,job_id,priority,instruction_num,all_instructions);
				}
			}
		});
		
		//添加按钮监听
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fra.setVisible(false);
			}
		});
	}
	
	private void SaveFile(File file,boolean if_first,int id,int priority,int instruction_num,ArrayList<Integer>all_instructions)
	{
		//保存文件
		try
		{
			FileWriter fw = new FileWriter(file,true);
			if(if_first==false)
				fw.write("\n\n"); 		//写入换行
			fw.write("job_id="+id+"\n");		//JCB的ID
			fw.write("priority="+priority+"\n");	//JCB的priority
			fw.write("instruction_num="+instruction_num+"\n");	//JCB的指令数目
			for(int i=0;i<instruction_num;i++)		//JCB的指令
				if(i!=0)
					fw.write(","+all_instructions.get(i));
				else
					fw.write(""+all_instructions.get(i));
			fw.close();
		}catch (IOException e){}
	}
}

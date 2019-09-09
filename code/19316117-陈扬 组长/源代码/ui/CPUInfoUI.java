package ui;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import hardware.*;
import os.kernel;

class MainPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	public JLabel label_int = new JLabel();
	public JLabel label_time = new JLabel();
	public JLabel label_time_data = new JLabel();
	MainPanel()
	{
		this.setBorder(BorderFactory.createTitledBorder("Timer:"));	//设置面板边界线
		this.setLayout(null);
		label_int.setFont(new Font("微软雅黑",Font.PLAIN,25));
		label_int.setBounds(25,10,180,100);
		label_int.setText("中断指示信号:");
		this.add(label_int);
		label_time.setFont(new Font("微软雅黑",Font.PLAIN,25));
		label_time.setBounds(25,150,170,100);
		label_time.setText("当前系统时间:");
		this.add(label_time);
		label_time_data.setFont(new Font("Times New Romar",Font.PLAIN,25));
		label_time_data.setBounds(200,150,190,100);
		this.add(label_time_data);
	}
	
	public void display()
	{
		this.repaint();
	}
	
	public void paint(Graphics g)
	{
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		label_time_data.setText(""+kernel.SYSTEM_TIME);
		if(CPU.cpu.ti.GetIfInterrupt())
		{
			g2d.setColor(Color.RED);
			g2d.fillRect(200, 20, 90, 90);
		}
		else
		{
			g2d.setColor(Color.GREEN);
			g2d.fillRect(200, 20, 90, 90);
		}
	}
}

public class CPUInfoUI extends JFrame{
	private static final long serialVersionUID = 1L;
	public static CPUInfoUI cpu_info_ui = new CPUInfoUI();
	private JButton fra_close=new JButton("关闭");
	MainPanel main_panel = new MainPanel();
	
	CPUInfoUI()
	{
		this.setLayout(null);
		this.setTitle("CPU信息");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		main_panel.setBounds(0,0,385,250);
		this.getContentPane().add(main_panel);
		this.getContentPane().add(fra_close);
		fra_close.setBounds(290, 260, 80, 40);
		fra_close.setFont(new Font("微软雅黑",Font.BOLD,14));
		fra_close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SetVisible(false);
			}
		});
		this.setSize(400, 350);
		this.setResizable(false); 	//窗口不可调整大小
		this.setLocationRelativeTo(null);	//默认居中显示
		this.setVisible(false);
	}
	
	public void SetVisible(boolean visible)
	{
		this.setVisible(visible);
	}
	
	public void RefreshData()
	{
		main_panel.display();
	}
}

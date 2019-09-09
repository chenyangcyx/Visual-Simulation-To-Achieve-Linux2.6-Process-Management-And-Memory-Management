package ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.table.*;

import os.*;

class panel_top extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public JLabel label_memory = new JLabel("内存使用情况:");
	public JLabel label_disk = new JLabel("虚存使用情况:");
	public JLabel label_hard = new JLabel("磁盘使用情况:");
	//public JLabel label_room = new JLabel("空间使用情况:");
	short i=0;
	
	public void display()
	{
		this.repaint();
	}
	
	panel_top()
	{
		this.setBorder(BorderFactory.createTitledBorder("空间使用情况:"));	//设置面板边界线
		this.setLayout(null);
		//this.add(label_room);
		//label_room.setBounds(2,5,85,25);
		label_memory.setBounds(5,20,85,20);
		this.add(label_memory);
		label_disk.setBounds(5,40,85,20);
		this.add(label_disk);
		label_hard.setBounds(5, 60, 85, 20);
		this.add(label_hard);		
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(Color.blue);
		//g2d.drawLine(90, 18, 490, 18);
		for(i=0;i<32;i++)
		{
			g2d.setColor(Color.RED);
			g2d.fillRect(90+i*10, 20, 10, 18);
		}
		for(i=32;i<64;i++)
		{
			if(PageModule.page_module.isBlockUsing(5, i))
			{
				g2d.setColor(new Color(0xff9900));
				g2d.fillRect(90+i*10, 20, 10, 18);
			}
			else
			{
				g2d.setColor(new Color(0x007FFF));
				g2d.fillRect(90+i*10, 20, 10, 18);
			}
		}
		for(i=64;i<192;i++) 
		{
			if(PageModule.page_module.isPageUsing(i))
			{
				g2d.setColor(new Color(0xff9900));
				g2d.fillRect(90+(i-64)*5, 40, 5, 18);
			}
			else
			{
				g2d.setColor(new Color(0x007FFF));
				g2d.fillRect(90+(i-64)*5, 40, 5, 18);
			}
		}
		double disk_usage=0.23;//PageModule.page_module.GetFreePageNumInDisk()/1888;
		int disk_end_pixel=(int) (400*disk_usage);
		g2d.setColor(new Color(0xff9900));
		g2d.fillRect(90, 60, disk_end_pixel, 18);
		g2d.setColor(new Color(0x007FFF));
		g2d.fillRect(disk_end_pixel+90, 60, 640-disk_end_pixel, 18);
	}
}

class panel_left extends JPanel{
	private static final long serialVersionUID = 1L;
	//public JLabel label_buddy = new JLabel("伙伴算法:");
	public JLabel label_5_l  = new JLabel("2^5");
	public JLabel label_4_l  = new JLabel("2^4");
	public JLabel label_3_l  = new JLabel("2^3");
	public JLabel label_2_l  = new JLabel("2^2");
	public JLabel label_1_l  = new JLabel("2^1");
	public JLabel label_0_l  = new JLabel("2^0");
	short i,j;
	
	public void display()
	{
		this.repaint();
	}
	
	panel_left()
	{
		this.setBorder(BorderFactory.createTitledBorder("伙伴算法:"));	//设置面板边界线
		this.setLayout(null);
		label_5_l.setFont(new Font("微软雅黑",Font.PLAIN,15));
		label_4_l.setFont(new Font("微软雅黑",Font.PLAIN,15));
		label_3_l.setFont(new Font("微软雅黑",Font.PLAIN,15));
		label_2_l.setFont(new Font("微软雅黑",Font.PLAIN,15));
		label_1_l.setFont(new Font("微软雅黑",Font.PLAIN,15));
		label_0_l.setFont(new Font("微软雅黑",Font.PLAIN,15));
		//this.add(label_buddy);
		//label_buddy.setBounds(2,0,85,25);
		this.add(label_5_l);
		label_5_l.setBounds(5,30,35,30);
		this.add(label_4_l);
		label_4_l.setBounds(5,60,35,30);
		this.add(label_3_l);
		label_3_l.setBounds(5,90,35,30);
		this.add(label_2_l);
		label_2_l.setBounds(5,120,35,30);
		this.add(label_1_l);
		label_1_l.setBounds(5,150,35,30);
		this.add(label_0_l);
		label_0_l.setBounds(5,180,35,30);		
	}
	
	//007FFF为未使用，ff5600为使用中，00ff8b为未分配
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(new Color(0x007FFF));
		for(i=0;i<6;i++)
		{
			for(j=0;j<Math.pow(2,i);j+=2)
			{
				int width=(int) (320 / Math.pow(2,i));
				if(i==0&&PageModule.page_module.isBlockUsing(i, j))
				{
					g2d.setColor(new Color(0xff5600));
					g2d.fillRect(40+width*j, 35+i*30, width, 15);
				}
				else if(i==0&&!PageModule.page_module.isBlockUsing(i, j))
				{
					g2d.setColor(new Color(0x007FFF));
					g2d.fillRect(40+width*j, 35+i*30, width, 15);
				}
				else if(PageModule.page_module.isBlockUsing(i, j)||PageModule.page_module.isBlockUsing(i, j+1))
				{
					if(PageModule.page_module.isBlockUsing(i, j)&&!PageModule.page_module.isBlockUsing(i, j+1))
					{
						g2d.setColor(new Color(0xff5600));
						g2d.fillRect(40+width*j, 35+i*30, width, 15);
						g2d.setColor(new Color(0x007FFF));
						g2d.fillRect(40+width*(j+1), 35+i*30, width, 15);
					}
					else if(!PageModule.page_module.isBlockUsing(i, j)&&PageModule.page_module.isBlockUsing(i, j+1))
					{
						g2d.setColor(new Color(0xff5600));
						g2d.fillRect(40+width*(j+1), 35+i*30, width, 15);
						g2d.setColor(new Color(0x007FFF));
						g2d.fillRect(40+width*j, 35+i*30, width, 15);
					}
					else
					{
						g2d.setColor(new Color(0xff5600));
						g2d.fillRect(40+width*(j+1), 35+i*30, width, 15);
						g2d.setColor(new Color(0xff5600));
						g2d.fillRect(40+width*j, 35+i*30, width, 15);
					}
				}
				else if(!PageModule.page_module.isBlockUsing(i, j)&&!PageModule.page_module.isBlockUsing(i, j+1))
				{
					g2d.setColor(new Color(0x00ff8b));
					g2d.fillRect(40+width*j, 35+i*30, width, 15);
					g2d.fillRect(40+width*(j+1), 35+i*30, width, 15);
				}
				else
				{
					g2d.setColor(new Color(0x007FFF));
					g2d.fillRect(40+width*j, 35+i*30, width, 15);
					g2d.setColor(new Color(0x007FFF));
					g2d.fillRect(40+width*(j+1), 35+i*30, width, 15);
				}
			}
		}
	}
}

class panel_right extends JPanel{
	private static final long serialVersionUID = 1L;
	//public JLabel label_bitmap = new JLabel("bitmap:");
	public JLabel label_5_r = new JLabel("2^5");
	public JLabel label_4_r  = new JLabel("2^4");
	public JLabel label_3_r  = new JLabel("2^3");
	public JLabel label_2_r  = new JLabel("2^2");
	public JLabel label_1_r  = new JLabel("2^1");
	public JLabel label_0_r  = new JLabel("2^0");
	short i,j;
	
	public void display()
	{
		this.repaint();
	}
	
	panel_right()
	{
		label_5_r.setFont(new Font("微软雅黑",Font.PLAIN,15));
		label_4_r.setFont(new Font("微软雅黑",Font.PLAIN,15));
		label_3_r.setFont(new Font("微软雅黑",Font.PLAIN,15));
		label_2_r.setFont(new Font("微软雅黑",Font.PLAIN,15));
		label_1_r.setFont(new Font("微软雅黑",Font.PLAIN,15));
		label_0_r.setFont(new Font("微软雅黑",Font.PLAIN,15));
		this.setBorder(BorderFactory.createTitledBorder("bitmap:"));	//设置面板边界线
		this.setLayout(null);
		//this.add(label_bitmap);
		//label_bitmap.setBounds(2,0,85,25);
		this.add(label_5_r);
		label_5_r.setBounds(5,30,35,30);
		this.add(label_4_r);
		label_4_r.setBounds(5,60,35,30);
		this.add(label_3_r);
		label_3_r.setBounds(5,90,35,30);
		this.add(label_2_r);
		label_2_r.setBounds(5,120,35,30);
		this.add(label_1_r);
		label_1_r.setBounds(5,150,35,30);
		this.add(label_0_r);
		label_0_r.setBounds(5,180,35,30);
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(Color.blue);
		g2d.setFont(new Font("微软雅黑",Font.PLAIN,12));
		for(i=0;i<6;i++)
		{
			short width=40;
			for(j=0;j<32;j++)
			{
				if(PageModule.page_module.isBlockUsing(i, j))
				{
					g2d.setColor(Color.RED);
					g2d.drawString("1", width, 50+i*30);
					width+=8;
					if((j+1)%5==0)
					{
						g2d.drawString(" ", width, 50+i*30);
						width+=8;
					}
				}
				else
				{
					g2d.setColor(Color.blue);
					g2d.drawString("0", width, 50+i*30);
					width+=8;
					if((j+1)%5==0)
					{
						g2d.drawString(" ", width, 50+i*30);
						width+=8;
					}
				}
			}
		}
	}
}

class panel_search extends JPanel{
	private static final long serialVersionUID = 1L;
	public JLabel label_search = new JLabel("查询:");
	public JTextPane text = new JTextPane();
	public JButton button_search = new JButton("查询");
	private JScrollPane scroll;	
	
	public void display()
	{
		this.repaint();
	}
	
	panel_search()
	{
		this.setLayout(null);
		label_search.setFont(new Font("微软雅黑",Font.PLAIN,28));
		button_search.setFont(new Font("微软雅黑",Font.PLAIN,28));
		this.add(label_search);
		label_search.setBounds(5, 0,70,40);
		this.add(button_search);
		button_search.setBounds(285, 0, 90, 40);
		this.add(text);
		text.setBounds(75, 0, 200, 40);
		text.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		text.setFont(new Font("Serif", 0, 35));	
		setLisenter();
	}
	
	public void paint(Graphics g) 
	{
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(Color.blue);
	}
	
	public void setLisenter()
	{
		button_search.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				search();
			}
		});
	}

	public void search()
	{
		if(text.getText().equals(""))
		{
			JOptionPane.showMessageDialog(null, "请输入数字");
			return;
		}
		short page_num=0;
		page_num=(short) Integer.parseInt(text.getText());
		if(page_num<32||page_num>63)
		{
			text.setText("");
			JOptionPane.showMessageDialog(null, "输入错误");
			return;
		}
		Page page = PageModule.page_module.GetPage(page_num);
		//System.out.println(page_num);
		JFrame frame_page = new JFrame("第"+page_num+"页信息查看");
		JPanel panel=new JPanel();					//面板：内存数据展示
		JTable data_table;		
		String []column_name= {"地址","00","01","02","03","04","05","06","07",
				"08","09","0A","0B","0C","0D","0E","0F"};	//设置表格列
		Object [][]row_data= new Object[32][17];		//设置表格行信息
		data_table=new JTable(row_data,column_name) {		//创建表格
			private static final long serialVersionUID = 1L;

			public boolean isCellSelected(int row, int column){
				if(column==0)return false;else return super.isCellSelected(row, column);}};
		scroll=new JScrollPane(data_table);					//使得表格可滚动下拉
		data_table.setFont(new Font("Courier New",Font.BOLD,14));	//设置表格内容字体
		JTableHeader jTableHeader = data_table.getTableHeader();	//获取表头
		jTableHeader.setFont(new Font("微软雅黑",Font.BOLD,12));	//设置表头名称字体样式
		jTableHeader.setResizingAllowed(false);	//设置用户是否可以通过在头间拖动来调整各列的大小
		jTableHeader.setReorderingAllowed(false);	//设置用户是否可以拖动列头，以重新排序各列
		DefaultTableCellRenderer cr = new DefaultTableCellRenderer();
		cr.setHorizontalAlignment(JLabel.CENTER);		//设置表格内容居中显示
		data_table.setDefaultRenderer(Object.class, cr);
        data_table.setModel(new DefaultTableModel(row_data,column_name) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row,int column){
        		return false;}});		//设置表格内容不可编辑
		data_table.getColumnModel().getColumn(0).setPreferredWidth(140);	//设置第0列的列宽
		data_table.setSelectionBackground(new Color(255,174,136,100));		//设置被选中后行背景
		
		panel.setBorder(BorderFactory.createTitledBorder("页面数据"));	//设置面板边界线
		panel.setLayout(null); 		//设置面板的排列方式
		panel.add(scroll);
		scroll.setBounds(5, 20, 670, 540);	//设置表格的绝对位置
		
		//设置第一列的值
		for(int i=0;i<32;i++)
		{
			data_table.setValueAt(String.format("%03x", i).toUpperCase()+"0",i,0);
			for(int j=1;j<16;j+=2)
			{
				data_table.setValueAt((page.GetPageData((short) (i*8+j))&0x00ff), i, j);
				data_table.setValueAt((page.GetPageData((short) (i*8+j))&0xff00), i, j+1);
			}
		}					//用来展示数据的表格
		
		frame_page.add(panel);
		frame_page.setVisible(true);
		frame_page.setSize(700, 610);
	}
}

class panel_button extends JPanel{
	private static final long serialVersionUID = 1L;
	public JButton button_save_image = new JButton("保存图像");
	public JButton button_refresh = new JButton("刷新");
	public JButton button_close = new JButton("关闭");
	
	public void display()
	{
		this.repaint();
	}
	
	panel_button()
	{
		button_save_image.setFont(new Font("微软雅黑",Font.PLAIN,26));
		button_refresh.setFont(new Font("微软雅黑",Font.PLAIN,26));
		button_close.setFont(new Font("微软雅黑",Font.PLAIN,26));
		this.setLayout(null);
		this.add(button_save_image);
		button_save_image.setBounds(405, 330, 140, 40);
		this.add(button_refresh);
		button_refresh.setBounds(545, 330, 100, 40);
		this.add(button_close);
		button_close.setBounds(645, 330, 100, 40);
		setListener();
	}
	
	public void paint(Graphics g) 
	{
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(Color.blue);
	}
	
	public void setListener() {
		button_close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				PageModuleUI.page_ui.SetVisable(false);	//关闭
			}
		});
		button_save_image.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				save_image();
			}
		});
		button_refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				refresh();
			}
		});
	}
	
	public void save_image()
	{
		PageModuleUI.page_ui.save_image();
		
	}
	
	public void refresh()
	{
		PageModuleUI.page_ui.refresh();
	}
}


public class PageModuleUI extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static PageModuleUI page_ui=new PageModuleUI();
	File file=null;
	FileDialog fileDialog=null;
	
	public void save_image() 
	{
		Container content = frame.getContentPane();
		content.setBounds(0, 0, frame.getWidth(), frame.getHeight());
		BufferedImage img = new BufferedImage(frame.getWidth(),frame.getHeight(),BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = img.createGraphics();
		content.paintAll(g2d);
		File file;
		fileDialog=new FileDialog(frame,"保存",FileDialog.SAVE);
		fileDialog.setVisible(true);
		if ((fileDialog.getDirectory()!=null) && (fileDialog.getFile()!=null))
        {
			//文件对象的赋值
            file= new File(fileDialog.getDirectory(),fileDialog.getFile());
    		try { 
    			ImageIO.write(img, "jpg", file);
    		}catch(IOException e) {
    			e.printStackTrace();
    		}
        }
		g2d.dispose();
	}
	
	public void refresh()
	{
		this.repaint();
	}
	
	public void SetVisable(boolean visable)
	{
		//设置该界面是否可视化
		this.frame.setVisible(visable);
	}
	
	panel_top top = new panel_top();
	panel_left left = new panel_left();
	panel_right right= new panel_right();
	panel_search search = new panel_search();
	panel_button button = new panel_button();
	JFrame frame = new JFrame("空间情况信息");
	PageModuleUI()
	{		
		frame.setBounds(0, 0, 800, 450);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setFont(new Font("微软雅黑",Font.PLAIN,13));
		frame.setResizable(false); 	//窗口不可调整大小
		frame.setLocationRelativeTo(null);	//默认居中显示
		
		top.setBounds(0, 0, 793, 80);
		frame.getContentPane().add(top);
		left.setBounds(0, 90, 400, 220);
		frame.getContentPane().add(left);
		right.setBounds(400, 90, 393, 220);
		frame.getContentPane().add(right);
		search.setBounds(0, 330, 400, 50);
		frame.getContentPane().add(search);
		button.setBounds(400, 330, 400, 50);
		frame.getContentPane().add(button);
		frame.setVisible(false);
		RefreshData();
	}
	
	public void RefreshData()
	{
		top.display();
		left.display();
		right.display();
		search.display();
		button.display();
	}
	
}

package ui;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.*;
import hardware.*;
import os.kernel;

public class HardDiskUI
{
	public static HardDiskUI hd_ui=new HardDiskUI();
	
	private JFrame fra=new JFrame("磁盘硬件");				//程序框架
	private JPanel panel=new JPanel();					//面板：磁盘数据展示
	private JTable data_table;							//用来展示数据的表格
	private JScrollPane scroll;							//表单的滚动
	private JLabel address_text=new JLabel("点击任一单元格查看详情 ");		//地址信息
	private JLabel data_text=new JLabel();							//数据信息
	private JButton button_save=new JButton("保存");		//按钮-保存到文件
	File file=null;
	FileDialog fileDialog=null;
	private JButton button_close=new JButton("关闭");		//按钮-关闭
	
	private JLabel choose_cylinder_label=new JLabel("磁道：");		//选择磁道的提示
	private JLabel choose_sector_label=new JLabel("扇区：");		//选择扇区的提示
	private JLabel cylinder_value=new JLabel("0");				//磁道的值
	private JLabel sector_value=new JLabel("0");				//扇区的值
	private JSlider cylinder_change=new JSlider(0,kernel.HARDDISK_CYLINDER_NUM,0);	//改变磁道的滑动条
	private JSlider sector_change=new JSlider(0,kernel.HARDDISK_SECTOR_NUM,0);		//改变扇区的滑动条
	
	private int column_num=16;										//表格列数
	private int row_num=kernel.HARDDISK_PAGE_SIZE/column_num;		//表格行数
	
	@SuppressWarnings("serial")
	HardDiskUI()
	{
		/*表格设置*/
		String []column_name= {"地址","00","01","02","03","04","05","06","07",
				"08","09","0A","0B","0C","0D","0E","0F"};	//设置表格列
		Object [][]row_data= new Object[row_num][column_num+1];		//设置表格行信息
		data_table=new JTable(row_data,column_name) {		//创建表格
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
        	public boolean isCellEditable(int row,int column){
        		return false;}});		//设置表格内容不可编辑
		data_table.getColumnModel().getColumn(0).setPreferredWidth(140);	//设置第0列的列宽
		data_table.setSelectionBackground(new Color(255,174,136,100));		//设置被选中后行背景
		/*表格设置*/
		
		/*磁盘数据面板设置*/
		panel.setBorder(BorderFactory.createTitledBorder("磁盘数据"));	//设置面板边界线
		panel.setLayout(null); 		//设置面板的排列方式
		panel.add(scroll);
		scroll.setBounds(18, 140, 544, 278);	//设置表格的绝对位置
		panel.add(address_text);		//地址信息
		panel.add(data_text);			//数据信息
		address_text.setBounds(20, 422, 400, 20);
		address_text.setFont(new Font("微软雅黑",Font.BOLD,15));
		data_text.setBounds(20, 447, 400, 20);
		data_text.setFont(new Font("微软雅黑",Font.BOLD,15));
		/*磁盘数据面板设置*/
		
		/*磁道扇区滑动条*/
		choose_cylinder_label.setFont(new Font("微软雅黑",Font.BOLD,15));
		choose_sector_label.setFont(new Font("微软雅黑",Font.BOLD,15));
		cylinder_value.setFont(new Font("微软雅黑",Font.BOLD,18));
		sector_value.setFont(new Font("微软雅黑",Font.BOLD,18));
		panel.add(choose_cylinder_label);
		panel.add(choose_sector_label);
		panel.add(cylinder_change);
		panel.add(sector_change);
		panel.add(cylinder_value);
		panel.add(sector_value);
		choose_cylinder_label.setBounds(18, 26, 50, 20);		//设置磁道信息文本的位置
		choose_sector_label.setBounds(18, 85, 50, 20);			//设置扇区信息文本的位置
		cylinder_change.setBounds(70,20,450,50);				//设置磁道滑动条的位置
		sector_change.setBounds(70,80,450,50);					//设置扇区滑动条的位置
		cylinder_value.setBounds(540,25,50,20);					//设置磁道值的位置
		sector_value.setBounds(540,85,50,20);					//设置扇区值的位置
		cylinder_change.setMajorTickSpacing(4);
		cylinder_change.setMinorTickSpacing(1);
		cylinder_change.setPaintTicks(true);
		cylinder_change.setPaintLabels(true);
		sector_change.setMajorTickSpacing(8);
		sector_change.setMinorTickSpacing(1);
		sector_change.setPaintTicks(true);
		sector_change.setPaintLabels(true);
		/*磁道扇区滑动条*/
		
		/*底部二个按钮*/
		fra.add(button_save);		//添加 “保存”按钮
		button_save.setBounds(445, 482, 65, 35);
		button_save.setFont(new Font("微软雅黑",Font.BOLD,14));
		fra.add(button_close);		//添加“关闭”按钮
		button_close.setBounds(510, 482, 65, 35);
		button_close.setFont(new Font("微软雅黑",Font.BOLD,14));
		/*底部二个按钮*/
		
		/*框架设置*/
		fra.setLayout(null);
		fra.add(panel);
		panel.setBounds(0, 0, 580, 477);	//设置面板的绝对位置
		fra.setVisible(true);
		fra.setResizable(false); 	//窗口不可调整大小
		SetColumn0Name();			//刷新第一列的值
		RefreshData();				//初始化所有单元格的值
		fra.setSize(600, 562);
		fra.setVisible(false);
		/*框架设置*/
		
		//添加按钮的触发器
		AddSliderChangeAction();		//滑动条的触发器
		AddButtonAction();				//下面三个按钮的触发器
		AddTableSelectionListener();	//表格选中的触发器
	}
	
	public void SetVisable(boolean visable)
	{
		//设置该界面是否可视化
		this.fra.setVisible(visable);
	}
	
	public void AddSliderChangeAction()
	{
		//增加滑动条的监听
		this.cylinder_change.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e)
			{
				//避免值为64
				if(cylinder_change.getValue()==kernel.HARDDISK_CYLINDER_NUM)
					cylinder_change.setValue(kernel.HARDDISK_CYLINDER_NUM-1);
				cylinder_value.setText(String.valueOf(cylinder_change.getValue()));
				RefreshData();
			}
		});
		this.sector_change.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e)
			{
				//避免值为32
				if(sector_change.getValue()==kernel.HARDDISK_SECTOR_NUM)
					sector_change.setValue(kernel.HARDDISK_SECTOR_NUM-1);
				sector_value.setText(String.valueOf(sector_change.getValue()));
				RefreshData();
			}
		});
	}
	
	public void SetColumn0Name()
	{
		//设置第一列的值
		for(int i=0;i<row_num;i++)
		{
			this.data_table.setValueAt(String.format("%03x", i).toUpperCase()+"0",i,0);
		}
	}
	
	public int CreateHardDiskAddress(int cylinder,int sector,int row,int column)
	{
		//生成磁盘地址
		return HardDisk.harddisk.CreateAddress(cylinder, sector, row*column_num+column);
	}
	
	public void RefreshData()
	{
		//刷新页面数据
		for(int i=0;i<row_num;i++)
		{
			for(int j=1;j<column_num+1;j++)
			{
				if(j%2!=0)
					this.data_table.setValueAt(String.format("%02x",
							(HardDisk.harddisk.GetData(CreateHardDiskAddress(cylinder_change.getValue(),sector_change.getValue(),
									i,j-1))>>8)&0xFF).toUpperCase(), i, j);
				else
					this.data_table.setValueAt(String.format("%02x",
							HardDisk.harddisk.GetData(CreateHardDiskAddress(cylinder_change.getValue(),sector_change.getValue(),
									i,j-1))&0xFF).toUpperCase(),i,j);
			}
		}
	}
	
	public void AddTableSelectionListener()
	{
		//添加表格选中监听器
		data_table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e)
			{
				int row=data_table.getSelectedRow();		//选中的行
				int col=data_table.getSelectedColumn();		//选中的列
				if(col!=0)
				{
					address_text.setText(
							"磁道："+cylinder_change.getValue()+
							"，扇区："+sector_change.getValue()+
							"，偏移：0x"+String.format("%04x",row*column_num+col-1).toUpperCase());
					if(col%2!=0)
						data_text.setText(
								"数据：0x"+String.format("%02x",(HardDisk.harddisk.GetData(CreateHardDiskAddress(cylinder_change.getValue(),
										sector_change.getValue(),row,col-1))>>8)&0xFF).toUpperCase()
								+"，双字数据：0x"+String.format("%04x", HardDisk.harddisk.GetData(CreateHardDiskAddress(cylinder_change.getValue(),
										sector_change.getValue(),row,col-1))).toUpperCase());
					else
						data_text.setText(
								"数据：0x"+String.format("%02x",HardDisk.harddisk.GetData(CreateHardDiskAddress(cylinder_change.getValue(),
										sector_change.getValue(),row,col-1))&0xFF).toUpperCase()
								+"，双字数据：0x"+String.format("%04x", HardDisk.harddisk.GetData(CreateHardDiskAddress(cylinder_change.getValue(),
										sector_change.getValue(),row,col-1))).toUpperCase());
				}
			}
		});
	}
	
	public void AddButtonAction()
	{
		//添加“保存”按钮功能
		this.button_save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
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
		//添加“关闭”按钮功能
		this.button_close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				SetVisable(false);	//关闭
			}
		});
	}

	private void SaveFile(File file)
	{
		//保存文件
		try
		{
			FileWriter fw = new FileWriter(file);
			fw.write("----磁盘硬件----\n");		//文件表头信息
			fw.write("保存时间："+CPU.cpu.ti.GetCurrentTime()+"      机器时间："+kernel.SYSTEM_TIME+"\n\n");	//写入时间
			for(int i=0;i<kernel.HARDDISK_CYLINDER_NUM;i++)
			{
				for(int j=0;j<kernel.HARDDISK_SECTOR_NUM;j++)
				{
					fw.write("磁道："+i+"，扇区："+j+"\n");
					for(int k=0;k<row_num;k++)
					{
						fw.write(String.format("%04x", k).toUpperCase()+"      ");
						for(int m=0;m<column_num;m++)
						{
							if(m%2==0)
								fw.write(String.format("%02x", (HardDisk.harddisk.GetData(CreateHardDiskAddress(i,j,k,m))>>8)&0xFF).toUpperCase());
							else
								fw.write(String.format("%02x", HardDisk.harddisk.GetData(CreateHardDiskAddress(i,j,k,m))&0xFF).toUpperCase());
							if(m!=column_num-1)
								fw.write("  ");
							else
								fw.write("\n");
						}
					}
					fw.write("\n");
				}
			}
			fw.close();
		}catch (IOException e){}
	}
}

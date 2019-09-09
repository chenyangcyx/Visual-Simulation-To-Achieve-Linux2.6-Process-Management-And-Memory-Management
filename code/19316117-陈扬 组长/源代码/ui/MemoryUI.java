package ui;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.table.*;
import hardware.*;
import os.kernel;

public class MemoryUI
{
	public static MemoryUI mem_ui=new MemoryUI();
	
	private JFrame fra=new JFrame("内存硬件");				//程序框架
	private JPanel panel=new JPanel();					//面板：内存数据展示
	private JTable data_table;							//用来展示数据的表格
	private JScrollPane scroll;							//表单的滚动
	private JLabel address_text=new JLabel("点击任一单元格查看详情 ");		//地址信息
	private JLabel data_text=new JLabel();							//数据信息
	private JButton button_save=new JButton("保存");		//按钮-保存到文件
	File file=null;
	FileDialog fileDialog=null;
	private JButton button_close=new JButton("关闭");		//按钮-关闭
	
	private int column_num=16;								//表格列数
	private int row_num=kernel.MEMORY_SIZE/column_num;		//表格行数
	
	@SuppressWarnings("serial")
	MemoryUI()
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
		
		/*内存数据面板设置*/
		panel.setBorder(BorderFactory.createTitledBorder("内存数据"));	//设置面板边界线
		panel.setLayout(null); 		//设置面板的排列方式
		panel.add(scroll);
		scroll.setBounds(18, 25, 544, 278);	//设置表格的绝对位置
		panel.add(address_text);		//地址信息
		panel.add(data_text);			//数据信息
		address_text.setBounds(20, 310, 400, 20);
		address_text.setFont(new Font("微软雅黑",Font.BOLD,15));
		data_text.setBounds(20, 335, 400, 20);
		data_text.setFont(new Font("微软雅黑",Font.BOLD,15));
		/*内存数据面板设置*/
		
		/*底部二个按钮*/
		fra.add(button_save);		//添加 “保存”按钮
		button_save.setBounds(445, 370, 65, 35);
		button_save.setFont(new Font("微软雅黑",Font.BOLD,14));
		fra.add(button_close);		//添加“关闭”按钮
		button_close.setBounds(510, 370, 65, 35);
		button_close.setFont(new Font("微软雅黑",Font.BOLD,14));
		/*底部二个按钮*/
		
		/*框架设置*/
		fra.setLayout(null);
		fra.add(panel);
		panel.setBounds(0, 0, 580, 364);	//设置面板的绝对位置
		fra.setVisible(true);
		fra.setResizable(false); 	//窗口不可调整大小
		SetColumn0Name();			//刷新第一列的值
		RefreshData();				//初始化所有单元格的值
		fra.setSize(600, 450);
		fra.setVisible(false);
		/*框架设置*/
		
		//添加按钮的触发器
		AddButtonAction();				//下面三个按钮的触发器
		AddTableSelectionListener();	//表格选中的触发器
	}
	
	public void SetVisable(boolean visable)
	{
		//设置该界面是否可视化
		this.fra.setVisible(visable);
	}
	
	public void SetColumn0Name()
	{
		//设置第一列的值
		for(int i=0;i<row_num;i++)
		{
			this.data_table.setValueAt(String.format("%03x", i).toUpperCase()+"0",i,0);
		}
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
							(Memory.memory.GetData((short) (i*column_num+j-1))>>8)&0x00FF).toUpperCase(), i, j);
				else
					this.data_table.setValueAt(String.format("%02x",
							Memory.memory.GetData((short) (i*column_num+j-1))&0x00FF).toUpperCase(), i, j);
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
				String row_str=String.format("%03x", data_table.getSelectedRow()).toUpperCase();
				String column_str=data_table.getColumnName(data_table.getSelectedColumn());
				if(col!=0)
				{
					address_text.setText(
							"行："+row_str+"0"
							+"，列："+column_str
							+"，地址：0x"+row_str+column_str.charAt(1));
					if(col%2!=0)
						data_text.setText(
								"数据：0x"+String.format("%02x",(Memory.memory.GetData((short) (row*column_num+col-1))>>8)&0xFF).toUpperCase()
								+"，双字数据：0x"+String.format("%04x", Memory.memory.GetData((short) (row*column_num+col-1))).toUpperCase());
					else
						data_text.setText(
								"数据：0x"+String.format("%02x",Memory.memory.GetData((short) (row*column_num+col-1))&0xFF).toUpperCase()
								+"，双字数据：0x"+String.format("%04x", Memory.memory.GetData((short) (row*column_num+col-1))).toUpperCase());
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
			fw.write("----内存硬件----\n");		//文件表头信息
			fw.write("保存时间："+CPU.cpu.ti.GetCurrentTime()+"      机器时间："+kernel.SYSTEM_TIME+"\n\n");	//写入时间
			for(int i=0;i<row_num;i++)
			{
				fw.write(String.format("%03x", i).toUpperCase()+"0    ");
				for(int j=0;j<column_num;j++)
				{
					if(j%2!=0)
						fw.write(String.format("%02x",(Memory.memory.GetData((short) (i*column_num+j))>>8)&0xFF).toUpperCase());
					else
						fw.write(String.format("%02x",Memory.memory.GetData((short) (i*column_num+j))&0xFF).toUpperCase());
					if(j!=15)
						fw.write("  ");
					else
						fw.write("\n");
				}
			}
			fw.close();
		}catch (IOException e){}
	}
}

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import hardware.*;
import os.*;
import ui.*;

public class Main {
	public static void main(String[] args)
	{

		JFrame frame = new JFrame("测试信息输出结果");
		JTextArea text = new JTextArea();
		JScrollPane sp = new JScrollPane(text);
		frame.setSize(640,480);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
		frame.add(sp);
		
		
		//测试页面调度
		String info = PageModule.page_module.ApplyPageInMemory((short) 7);
		int aft = PageModule.page_module.GetFreePageNumInMemory();
		text.append("还有几个可用块"+aft+"\n");
		text.append("分配到的链表号和块号"+info+"\n");
		String info2 = PageModule.page_module.ApplyPageInMemory((short) 4);
		aft = PageModule.page_module.GetFreePageNumInMemory();
		text.append("还有几个可用块"+aft+"\n");
		text.append("分配到的链表号和块号"+info2+"\n");
		String info3 = PageModule.page_module.ApplyPageInMemory((short) 20);
		aft = PageModule.page_module.GetFreePageNumInMemory();
		text.append("还有几个可用块"+aft+"\n");
		text.append("分配到的链表号和块号"+info3+"\n");
		PageModule.page_module.RecyclePage((short) 32);
		PageModule.page_module.RecyclePage((short) 33);
		PageModule.page_module.RecyclePage((short) 34);
		PageModule.page_module.RecyclePage((short) 35);
		PageModule.page_module.RecyclePage((short) 36);
		PageModule.page_module.RecyclePage((short) 37);
		PageModule.page_module.RecyclePage((short) 38);
		PageModule.page_module.RecyclePage((short) 39);
		aft = PageModule.page_module.GetFreePageNumInMemory();
		text.append("还有几个可用块"+aft+"\n");
		aft = PageModule.page_module.GetFreePageNumInDisk();
		text.append("还有几个可用块"+aft+"\n");
		info = PageModule.page_module.ApplyPageInDisk((short) 5);
		text.append("分配到的块号"+info+"\n");
		aft = PageModule.page_module.GetFreePageNumInDisk();
		text.append("还有几个可用块"+aft+"\n");
		info2 = PageModule.page_module.ApplyPageInDisk((short) 8);
		aft = PageModule.page_module.GetFreePageNumInDisk();
		text.append("还有几个可用块"+aft+"\n");
		text.append("分配到的块号"+info2+"\n");
		aft = PageModule.page_module.GetFreePageNumInDisk();
		text.append("还有几个可用块"+aft+"\n");
		PageModule.page_module.RecyclePage((short) 64);
		PageModule.page_module.RecyclePage((short) 65);
		PageModule.page_module.RecyclePage((short) 66);
		aft = PageModule.page_module.GetFreePageNumInDisk();
		text.append("还有几个可用块"+aft+"\n");
		PageModuleUI.page_ui.SetVisable(true);
		PageModuleUI.page_ui.RefreshData();
		int bef = PageModule.page_module.GetFreePageNumInMemory();
		info = PageModule.page_module.ApplyPageInMemory((short) 7);
		aft = PageModule.page_module.GetFreePageNumInMemory();
		short next = PageModule.page_module.GetOneFreePageInMemory();
		text.append("分配到的链表号和块号"+info+"\n");
		text.append("还有几个可用块"+aft+"\n");
		text.append("分配后少了几个可用块"+(bef-aft)+"\n");
		text.append("下一个可用块的块号"+next+"\n");
		CPUInfoUI.cpu_info_ui.SetVisible(true);
		
		
		
		//测试页面
		Page page = new Page((short) 40);
		Page test = new Page((short) 39);
		page.SetPageData((short) 0,(short)  233);
		test.SetPageData((short) 0,(short)  2333);
		text.append("第40页内存地址0处的数据"+Memory.memory.GetData((short) (CPU.cpu.mm.PageToRealAddress((short) 40)))+"\n");
		text.append("第39页内存地址0处的数据"+Memory.memory.GetData((short) (CPU.cpu.mm.PageToRealAddress((short) 39)))+"\n");
		text.append("页40中数据"+page.GetPageData((short) 0)+"\n");
		text.append("页39中数据"+test.GetPageData((short) 0)+"\n");
		PageModule.page_module.ExchangePage((short) 40, (short) 39);
		text.append("交换后页40中数据"+page.GetPageData((short) 0)+"\n");
		text.append("交换后页39中数据"+test.GetPageData((short) 0)+"\n");
		text.append("第40页内存地址0处的数据"+Memory.memory.GetData((short) (CPU.cpu.mm.PageToRealAddress((short) 40)))+"\n");
		text.append("第39页内存地址0处的数据"+Memory.memory.GetData((short) (CPU.cpu.mm.PageToRealAddress((short) 39)))+"\n");
		Page page_new = new Page((short) 40);
		Page test_new = new Page((short) 39);
		text.append("交换后重新生成页40中数据"+page_new.GetPageData((short) 0)+"\n");
		text.append("交换后重新生成页39中数据"+test_new.GetPageData((short) 0)+"\n");

		info = PageModule.page_module.ApplyPageInMemory((short) 4);
		aft=PageModule.page_module.GetFreePageNumInMemory();
		text.append("还有几个可用块"+aft+"\n");
		text.append("分配到的链表号和块号"+info+"\n");
		Page buddy_page_test = new Page((short) 32);
		buddy_page_test.SetPageData((short) 0,(short)  123);
		text.append("页32中数据"+buddy_page_test.GetPageData((short) 0)+"\n");
		text.append("第32页内存地址0处的数据"+Memory.memory.GetData((short) (CPU.cpu.mm.PageToRealAddress((short) 32)))+"\n");
		PageModule.page_module.RecyclePage((short) 32);
		aft=PageModule.page_module.GetFreePageNumInMemory();
		next = PageModule.page_module.GetOneFreePageInMemory();
		text.append("回收后还有几个可用块"+aft);
		text.append("下一个可用块的块号"+next);
		text.append("释放后页32中数据"+buddy_page_test.GetPageData((short) 0)+"\n");
		text.append("第32页内存地址0处的数据"+Memory.memory.GetData((short) (CPU.cpu.mm.PageToRealAddress((short) 32)))+"\n");
		Page page_new2 = new Page((short) 32);
		text.append("释放后重新生成页32中数据"+page_new2.GetPageData((short) 0)+"\n");
		text.append("内存中的数据"+Memory.memory.GetData((short) (CPU.cpu.mm.PageToRealAddress((short) 0)))+"\n");

		
	}
}

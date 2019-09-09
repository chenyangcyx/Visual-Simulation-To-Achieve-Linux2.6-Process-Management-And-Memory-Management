# Visual Simulation To Achieve Linux2.6 Process Management And Memory Management
可视化仿真实现Linux2.6进程管理与内存管理，计算机操作系统课程设计

Abstract

In order to test the learning situation and mastery of our own operating system course, as well as the skills to describe the course knowledge in programming language, this group chose the topic of "visual simulation to realize Linux2.6 process management and memory management" to design the operating system course.The course design plays an important role in understanding the knowledge of process management and memory management in the operating system. Meanwhile, the management method adopts the rules of Linux2.6 kernel, which can take this opportunity to check my programming level and reading level of Linux core code.

System simulation there system, and implements the operation and process concurrent environment, MMU address transformation, process primitives, page table is generated with the page scheduling algorithm, job scheduling process and algorithm (job scheduling to the instruction set, at least three states transformation), page distribution and recovery algorithms, process synchronization process mutex, deadlock detection and cancellation algorithm, through the visual way to present and will implement the principle of the process.

According to the computer theory, in the program framework, the system is divided into four modules: hardware, driver, system management module and UI interface.

At the hardware level, the system designs four hardware including CPU, memory, external storage (hard disk) and address line data line according to the needs of the problem. The CPU also includes timer and MMU.The CPU is responsible for the execution of process instructions and the transfer of data, the timer is responsible for the calculation of interruption and system time, and the MMU is responsible for the change of address in the system.Memory and external storage are storage devices in the system, and all jobs, processes, and page management are designed based on these two hardware.

The system management module is divided into job management, process management and page management.Job management module is one of the system modules based on CPU and hard disk hardware.The function of this module is to provide related support for the creation, storage, deletion of jobs and the call-in detection of jobs.The job management module is written in the jobmodule.java file in the Java project, and the class is called by control.java.

Process management module is one of the system modules based on CPU and memory.The function of this module is to provide administrative functions for the processes that the jobs are transferred into.Process management is the most complex part in the whole system design, including low-level scheduling management, intermediate scheduling management, advanced scheduling management, process list and other functions.The process management module is written in the processmodule.java file in the Java project, and the class is called by control.java.

Page management is responsible for the system for page storage, reading, switching, switching in and out of the function of the management module.In Page management, the exchange of information between the module and other modules is all conducted through the Page class.When a Page swap in or out request is sent, the module first writes the Page information to the object of Page class, and then sends out the object. When other modules receive the object, they can also operate on the object to reduce the complexity of the operation.The page management module is written in the PageModule. Java file in the Java project, and the class is called by control.java.

The UI interface of the system is an interface directly provided by Java programs to users for operation. Through this interface, users can easily and quickly use all system functions and know the real-time information of all function modules and hardware devices of the system.

To sum up, the system well realizes all the requirements of course design, and at the same time, it also provides a vivid UI interface for users to operate and manage.

Key words: Linux2.6, system simulation, process management, memory management, JAVA programming

1. Practical purpose and significance

In order to test my learning situation and mastery of the one-semester operating system course, as well as the skills to describe the course knowledge in programming language, I chose the topic of "visual simulation to realize Linux2.6 process management and memory management" to design the operating system course.

The course design plays an important role in understanding the knowledge of process management and memory management in the operating system. Meanwhile, the management method adopts the rules of Linux2.6 kernel, which can take this opportunity to check my programming level and reading level of Linux core code.

Practical tasks and cooperation

According to the principle of there process management and memory management, simulation operation and process concurrent environment, MMU address transformation, process primitives, page table is generated with the page scheduling algorithm, job scheduling process and algorithm (job scheduling to the instruction set, at least three states transformation), page distribution and recovery algorithms, process synchronization process mutex, deadlock detection and cancellation algorithm, through the visual way to present and will implement the principle of the process.

In order to achieve the above objectives, we have the following division of labor:

Group leader (Chen Yang) :

The conception and construction of the whole frame of the system

Programming specification writing

Three-stage scheduling process and algorithm

JCB, PCB design

Deadlock detection and undo algorithm

A page table to generate

Visualize the process

Processes synchronize the implementation of mutual exclusion

Design and implementation of process and process primitives

Team member (leung ka man) :

Simulation of CPU components

Simulation of memory space

MMU address transformation

Page design implementation

Page scheduling algorithm

Page allocation and recycling algorithm

Visualize the process

3 program structure description

The structure design of the system refers to the chapter of device management in the operating system textbook. From the bottom up, it can be divided into: hardware, hardware driver, system management module, system kernel, UI interface.At the same time, since the simulation of the hardware part itself contains the functions of storage and reading, the hardware and the hardware driver are combined.The system management module is divided into three parts: job management, process management and page management.In the kernel part of the system, the system provides global variables and operation classes of the system, which are used to integrate the three modules under it.Further up, the UI interface provided by JAVA can facilitate users to use the system.
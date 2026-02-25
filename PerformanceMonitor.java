import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.io.File;

public class PerformanceMonitor extends JFrame {

    private JProgressBar cpuBar, memoryBar, diskBar, batteryBar;
    private JLabel scoreLabel, energyLabel, statusLabel, systemLabel;
    private Timer timer;
    private boolean running = true;

    private OperatingSystemMXBean osBean;

    public PerformanceMonitor() {

        osBean = (OperatingSystemMXBean)
                ManagementFactory.getOperatingSystemMXBean();

        setTitle("Smart System Performance Analyzer - Ultimate Edition");
        setSize(700,550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(12,1,10,10));
        panel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        cpuBar = createBar("CPU Usage", panel);
        memoryBar = createBar("Memory Usage", panel);
        diskBar = createBar("Disk Usage (C Drive)", panel);
        batteryBar = createBar("Battery", panel);

        scoreLabel = new JLabel();
        energyLabel = new JLabel();
        statusLabel = new JLabel();
        systemLabel = new JLabel();

        panel.add(scoreLabel);
        panel.add(energyLabel);
        panel.add(statusLabel);
        panel.add(systemLabel);

        JButton exportBtn = new JButton("Export Report");
        JButton toggleBtn = new JButton("Pause / Resume");

        panel.add(exportBtn);
        panel.add(toggleBtn);

        add(panel);

        exportBtn.addActionListener(e -> exportReport());

        toggleBtn.addActionListener(e -> {
            if(running){
                timer.stop();
                running=false;
            }else{
                timer.start();
                running=true;
            }
        });

        updateSystemInfo();

        timer = new Timer(1000, e -> updateStats());
        timer.start();

        setVisible(true);
    }

    private JProgressBar createBar(String name, JPanel panel){

        JLabel label = new JLabel(name);
        JProgressBar bar = new JProgressBar(0,100);
        bar.setStringPainted(true);

        panel.add(label);
        panel.add(bar);

        return bar;
    }

    private void updateStats(){

        int cpu = getCPU();
        int mem = getMemory();
        int disk = getDisk();
        int battery = getBattery();

        updateBar(cpuBar,cpu);
        updateBar(memoryBar,mem);
        updateBar(diskBar,disk);
        updateBar(batteryBar,battery);

        int score = calculateScore(cpu,mem,disk);
        int energy = 100 - cpu;

        scoreLabel.setText("Performance Score: "+score+"/100");
        energyLabel.setText("Energy Efficiency: "+energy+"/100");

        statusLabel.setText("Status: "+getStatus(score));
    }

    private void updateBar(JProgressBar bar,int value){

        bar.setValue(value);
        bar.setString(value+"%");

        if(value<50)
            bar.setForeground(Color.GREEN);
        else if(value<80)
            bar.setForeground(Color.ORANGE);
        else
            bar.setForeground(Color.RED);
    }

    private int getCPU(){
        return (int)(osBean.getSystemCpuLoad()*100);
    }

    private int getMemory(){

        long total = osBean.getTotalMemorySize();
        long free = osBean.getFreeMemorySize();

        return (int)(((total-free)*100)/total);
    }

    private int getDisk(){

        File disk = new File("C:");

        long total = disk.getTotalSpace();
        long free = disk.getFreeSpace();

        return (int)(((total-free)*100)/total);
    }

    private int getBattery(){

        try{

            Process p = Runtime.getRuntime().exec(
                    "WMIC PATH Win32_Battery Get EstimatedChargeRemaining");

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(p.getInputStream()));

            reader.readLine();

            String value = reader.readLine();

            if(value!=null)
                return Integer.parseInt(value.trim());

        }catch(Exception e){}

        return 100;
    }

    private int calculateScore(int cpu,int mem,int disk){

        int score = 100;

        score -= cpu*0.4;
        score -= mem*0.3;
        score -= disk*0.3;

        if(score<0) score=0;

        return score;
    }

    private String getStatus(int score){

        if(score>=80) return "EXCELLENT";
        if(score>=60) return "GOOD";
        if(score>=40) return "FAIR";
        return "POOR";
    }

    private void updateSystemInfo(){

        systemLabel.setText(
                "OS: "+System.getProperty("os.name")+
                        " | Cores: "+osBean.getAvailableProcessors()+
                        " | RAM: "+
                        (osBean.getTotalMemorySize()/1024/1024/1024)+" GB"
        );
    }

    private void exportReport(){

        try{

            FileWriter writer =
                    new FileWriter("UltimateSystemReport.txt");

            writer.write("SMART SYSTEM PERFORMANCE ANALYZER REPORT\n");
            writer.write("----------------------------------------\n");

            writer.write(cpuBar.getString()+" CPU\n");
            writer.write(memoryBar.getString()+" Memory\n");
            writer.write(diskBar.getString()+" Disk\n");
            writer.write(batteryBar.getString()+" Battery\n");

            writer.write(scoreLabel.getText()+"\n");
            writer.write(energyLabel.getText()+"\n");
            writer.write(statusLabel.getText()+"\n");

            writer.close();

            JOptionPane.showMessageDialog(this,"Report Exported");

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args){

        SwingUtilities.invokeLater(() ->
                new PerformanceMonitor());
    }
}


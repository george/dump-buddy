package org.hostile.dumpbuddy;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class AgentInjector {

    public static void main(String[] args) throws IOException  {
        Class<?> clazz = AgentInjector.class;

        InputStream inputStream = clazz.getResourceAsStream("/attach.dll");

        byte[] bytes = new byte[inputStream.available()];
        int bytesRead = inputStream.read(bytes);

        System.out.printf("Read %d bytes from attach.dll resource%n", bytesRead);

        Files.write(Paths.get("attach.dll"), bytes);
        System.out.println("Wrote bytes to attach.dll");

        System.load(Paths.get("attach.dll").toFile().getAbsolutePath());
        System.out.println("Loaded attach library!");

        VirtualMachine.list().forEach(vm -> {
            System.out.println("Found vm " + vm.id() + " with display name " + vm.displayName());
        });

        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the process ID of the VM you would like to attach to");
        int targetVm = scanner.nextInt();

        System.out.println("Attempting to attach to VM with PID " + targetVm);

        VirtualMachine virtualMachine;

        try {
            virtualMachine = VirtualMachine.attach(Integer.toString(targetVm));
        } catch (AttachNotSupportedException exc) {
            System.out.println("An unexpected error occurred while attempting to attach to the target VM!");
            exc.printStackTrace();

            return;
        }

        System.out.println("Enter the path of the target agent");
        String agentPath = scanner.next();

        try {
            virtualMachine.loadAgentPath(agentPath);
        } catch (AgentLoadException | IOException | AgentInitializationException exc) {
            System.out.println("An unexpected error occurred while attempting to inject the agent!");
            exc.printStackTrace();

            return;
        }

        System.out.println("Successfully injected agent!");
    }
}

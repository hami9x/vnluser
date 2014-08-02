package rfx.server.util.scribe;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import rfx.server.util.scribe.scribe.Client;

public class ScribeLogUtil {
	public static void main(String[] args) throws Exception {
		args = new String[] { "192.168.10.221", "1463" };

		if (args.length != 2) {
			System.out.println(" <Host> <Port> missing");
			System.exit(1);
		}
		int port = -1;
		try {
			port = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			System.exit(1);
		}
		System.out.println(args[0]);
		System.out.println(args[1]);
		TTransport tr = new TFramedTransport(new TSocket(args[0], port));
		tr.open();
		TProtocol proto = new TBinaryProtocol(tr);
		Client client = new Client(proto);
		System.out.println(client.aliveSince());

		int i = 0;
		List<LogEntry> list = new ArrayList<LogEntry>();
		LogEntry log = null;
		while (i < 10000) {
			log = new LogEntry();
			log.setCategory("test2");
			log.setMessage("My Message " + i);
			list.add(log);
			i++;
		}
		try {

			ResultCode rc = client.Log(list);
			System.out.println(rc);

		} catch (org.apache.thrift.TException e) {
			e.printStackTrace();
		}
		Thread.sleep(5000);
	}
}

package minespy.nbt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;

public class Test {

	public static void main(String[] args) throws Exception {

		Tag root = Tag.parse(new DataInputStream(new FileInputStream("level.nbt")));

		root.child("Data", "Player").set("XpLevel", 9001);
		root.child("Data", "Player", "Motion").set(2, 9000.01);
		root.child("Data", "Player", "Motion").set(3, 42);
		root.child("Data", "Player", "Motion").set(4, "65536");
		root.child("Data", "Player").set("Health", 4.8);
		root.child("Data").set("thundering", true);
		root.set("FOO!", "baaaarrrrrrrrr");
		root.set("FOO!", 69);

		// Tag.print(root);

		ByteArrayOutputStream os = new ByteArrayOutputStream();

		root.write(new DataOutputStream(os));

		Tag root2 = Tag.parse(new DataInputStream(new ByteArrayInputStream(os.toByteArray())));

		Tag root3 = root2.clone();

		root2.clear();

		if (root3.child("Data").getBool("thundering")) {
			System.out.println("THUNDERING!!!");
		}

		double health = root3.child("Data", "Player").getDouble("Health");
		System.out.println(health);

		Tag.print(root2);
		Tag.print(root3);

	}
}

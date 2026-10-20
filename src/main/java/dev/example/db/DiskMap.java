package dev.example.db;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.ConcurrentHashMap;

public final class DiskMap<K extends Serializable, V extends Serializable>
        implements AutoCloseable {

    private final Path file;
    private final ConcurrentHashMap<K, V> data;

    public DiskMap(Path file) throws IOException {
        this.file = file;
        this.data = load(file);
    }

    public V get(K key) {
        return data.get(key);
    }

    public V put(K key, V value) throws IOException {
        V previous = data.put(key, value);
        save();
        return previous;
    }

    public V remove(K key) throws IOException {
        V previous = data.remove(key);
        save();
        return previous;
    }

    public int size() {
        return data.size();
    }

    public void clear() throws IOException {
        data.clear();
        save();
    }

    private void save() throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        Files.createDirectories(parent);

        Path temp = Files.createTempFile(parent, "db-", ".tmp");

        try (ObjectOutputStream out =
                     new ObjectOutputStream(Files.newOutputStream(temp))) {
            out.writeObject(data);
        }

        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @SuppressWarnings("unchecked")
    private static <K extends Serializable, V extends Serializable>
    ConcurrentHashMap<K, V> load(Path file) throws IOException {
        if (!Files.exists(file)) {
            return new ConcurrentHashMap<>();
        }

        try (ObjectInputStream in =
                     new ObjectInputStream(Files.newInputStream(file))) {
            return (ConcurrentHashMap<K, V>) in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Could not read database contents", e);
        }
    }

    @Override
    public void close() throws IOException {
        save();
    }
}
package Objects.MGitObjects;

import Objects.MiniGitRepository;
import Objects.DTO.TreeDTO;
import UtilityMethods.ParseTree;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

public class TreeObject extends MGitObject{
    private final String format = "tree";
    private List<TreeDTO> items;

    public TreeObject(byte[] data) {
        super(data);
        if (items == null) this.items = new ArrayList<>();
    }


    @Override
    public String serialize(MiniGitRepository repo) {
        items.sort(ParseTree.treeDTOComparator);

        try (var out = new ByteArrayOutputStream()) {
            for (TreeDTO item : items) {
                out.write(item.mode());
                out.write(' ');
                out.write(item.path());
                out.write(0);
                out.write(item.sha().getBytes());
            }
            byte[] content = out.toByteArray();
            return new String(content);
        } catch (IOException e) {
            throw new UncheckedIOException("Tree serialization failed", e);
        }
    }



    @Override
    public void deserialize(byte[] data) {
        this.items = ParseTree.parseTree(data);
    }

    @Override
    public List<TreeDTO> getContent() {
        return items;
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public String getFormat() {
        return format;
    }

    public void addItem(TreeDTO item) {
        this.items.add(item);
    }
}

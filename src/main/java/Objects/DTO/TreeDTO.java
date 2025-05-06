package Objects.DTO;

public record TreeDTO(byte[] mode, byte[] path, String sha) {
    public int dataLength() {
        return mode.length + path.length + 22;
    }
}

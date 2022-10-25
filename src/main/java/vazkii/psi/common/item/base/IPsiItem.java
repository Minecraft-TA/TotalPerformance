package vazkii.psi.common.item.base;

public interface IPsiItem {
    default String getModNamespace() {
        return "psi";
    }
}

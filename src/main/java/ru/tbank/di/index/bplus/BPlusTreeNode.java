package ru.tbank.di.index.bplus;

import java.util.ArrayList;
import java.util.List;

/**
 * B+-Tree узел с M позициями и M-1 ключами
 *
 * Нотация:
 * - M = 2t (количество позиций/детей)
 * - M-1 = 2t-1 (максимум ключей)
 * - Узел содержит 0 до M-1 ключей и 0 до M позиций-детей
 */
public class BPlusTreeNode<TID> {
    // ПАРАМЕТРЫ ПОРЯДКА
    // Если порядок t=50, то:
    // M = 2*50 = 100 позиций
    // M-1 = 99 ключей максимум
    static final int ORDER_T = 50;
    static final int M = 2 * ORDER_T;                  // 100 позиций
    static final int MAX_KEYS = M - 1;                // 99 ключей
    static final int MIN_KEYS = ORDER_T - 1;          // 49 ключей (кроме корня)
    static final int MIN_KEYS_ROOT = 1;               // корень может быть с 1 ключём

    // ДИСКОВАЯ СТРУКТУРА УЗЛА
    // Всё это хранится последовательно на странице (8KB)

    // === HEADER (50 байт) ===
    private int pageId;                    // ID страницы на диске
    private boolean isLeaf;                // true если это листовой узел
    private int numKeys;                   // текущее количество ключей (0 до M-1)
    private int parentPageId = -1;         // ID родителя (-1 если корень)
    private int leftSiblingPageId = -1;    // для листьев: предыдущий лист
    private int rightSiblingPageId = -1;   // для листьев: следующий лист

    // === МАССИВЫ ДАННЫХ ===
    // Инвариант: массивы ВСЕГДА отсортированы по ключам
    private List<Integer> keys;            // M-1 позиций (ключи)
    private List<Integer> childPageIds;    // M позиций для internal узлов
    private List<TID> tids;                // M-1 позиций для leaf узлов

    // КОНСТРУКТОР
    public BPlusTreeNode(int pageId, boolean isLeaf) {
        this.pageId = pageId;
        this.isLeaf = isLeaf;
        this.numKeys = 0;
        this.keys = new ArrayList<>();

        if (isLeaf) {
            // Листовой узел: хранит (ключ, TID) пары
            this.tids = new ArrayList<>();
            this.childPageIds = null;
        } else {
            // Internal узел: хранит (ключ, детей_указатель) пары
            this.childPageIds = new ArrayList<>();
            this.tids = null;
            // ВАЖНО: childPageIds имеет на ОДНОГО больше ребёнка, чем ключей!
            // Инвариант M-way: M детей, M-1 ключей
            // Поэтому P0, K0, P1, K1, P2, ...
            // childPageIds = [P0, P1, P2, ...] имеет M позиций
        }
    }

    // === GETTERS ===
    public int getPageId() { return pageId; }
    public boolean isLeaf() { return isLeaf; }
    public int getNumKeys() { return numKeys; }
    public List<Integer> getKeys() { return new ArrayList<>(keys); }
    public List<TID> getTids() { return new ArrayList<>(tids); }
    public List<Integer> getChildPageIds() { return new ArrayList<>(childPageIds); }
    public int getParentPageId() { return parentPageId; }
    public int getLeftSiblingPageId() { return leftSiblingPageId; }
    public int getRightSiblingPageId() { return rightSiblingPageId; }

    // === ПРОВЕРКИ ИНВАРИАНТОВ ===

    /**
     * Может ли узел вместить ещё один ключ?
     * Инвариант: numKeys <= MAX_KEYS
     */
    public boolean canInsert() {
        return numKeys < MAX_KEYS;
    }

    /**
     * Переполнен ли узел (нарушен инвариант)?
     * Происходит после вставки в уже полный узел, перед split'ом
     */
    public boolean isOverflow() {
        return numKeys > MAX_KEYS;
    }

    /**
     * Слишком ли мало ключей для merge (не применимо для этого семинара)
     */
    public boolean isUnderfull() {
        return numKeys < MIN_KEYS && parentPageId >= 0;
    }

    // === ПОИСК ПОЗИЦИИ ===

    /**
     * Найти индекс для вставки нового ключа (двоичный поиск)
     * Возвращает позицию i, куда нужно вставить key
     *
     * Инвариант M-way:
     * - Если key <= keys[0], позиция = 0
     * - Если keys[i] < key <= keys[i+1], позиция = i+1
     * - Если key > keys[numKeys-1], позиция = numKeys
     */
    public int findInsertPosition(int key) {
        // TODO bin search here
    }

    /**
     * Найти индекс ребёнка для спуска при поиске/вставке
     *
     * Инвариант M-way (ОЧЕНЬ ВАЖНО):
     * - Все значения < keys[0] → берём детей[0] (P0)
     * - Все значения в [keys[i], keys[i+1]) → берём детей[i+1] (P_{i+1})
     * - Все значения >= keys[numKeys-1] → берём детей[numKeys] (P_M)
     *
     * @param key искомый/вставляемый ключ
     * @return индекс в массиве childPageIds (от 0 до M)
     */
    public int findChildIndex(int key) {
        // TODO bin search here
    }

    // === SIBLING-ССЫЛКИ (для листьев) ===

    public void setLeftSibling(int pageId) { this.leftSiblingPageId = pageId; }
    public void setRightSibling(int pageId) { this.rightSiblingPageId = pageId; }
    public void setParent(int pageId) { this.parentPageId = pageId; }

    // === UTILITY ===

    @Override
    public String toString() {
        return String.format(
                "BPlusTreeNode{page=%d, isLeaf=%s, numKeys=%d/%d, keys=%s}",
                pageId, isLeaf, numKeys, MAX_KEYS, keys
        );
    }
}

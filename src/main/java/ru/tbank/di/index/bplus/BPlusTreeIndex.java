package ru.tbank.di.index.bplus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * B+-Tree индекс
 *
 * Используется M-way нотация:
 * - M позиций (детей)
 * - M-1 ключей максимум
 * - Все листья на одном уровне
 */
public class BPlusTreeIndex<TID> {

    // === ПАРАМЕТРЫ ===
    static final int MAX_KEYS = 99;  // M-1 = 99 (если t=50, M=100)
    static final int M = 100;         // позиций/детей

    // === СОСТОЯНИЕ ИНДЕКСА ===
    private int rootPageId;
    private int height;
    private int nextPageId = 0;

    // Для семинара: in-memory кеш страниц. В домашке будем делать по-настоящему
    private Map<Integer, BPlusTreeNode> pageCache = new HashMap<>();

    // КОНСТРУКТОР
    public BPlusTreeIndex() {
        // Создаём корневую страницу (это сразу и лист)
        BPlusTreeNode root = new BPlusTreeNode(nextPageId++, true);
        pageCache.put(root.getPageId(), root);

        this.rootPageId = root.getPageId();
        this.height = 1;
    }

    // === SEARCH: O(log n) ===

    public List<TID> search(int key) {
        // implement
    }

    // === RANGE SEARCH: O(log n + m) ===
    /**
     * Range scan: найти все TID'ы с ключами в [fromKey, toKey]
     *
     * РЕАЛИЗОВАНО НА ПРАКТИКЕ 3
     *
     * Алгоритм:
     * 1. Найти первый лист, где может быть fromKey
     * 2. Идти по листьям через right_sibling_page_id (вот это мощь B+-tree!)
     * 3. Собирать TID'ы, пока <= toKey
     */
    public List<TID> rangeSearch(int fromKey, int toKey) {
        // implement
    }

    /**
     * Спустить от корня к листу, выбирая правильного ребёнка
     *
     * Инвариант M-way:
     * - Если key < узел.keys[0], спускаемся в узел.children[0]
     * - Если узел.keys[i] <= key < узел.keys[i+1], спускаемся в узел.children[i+1]
     * - Иначе спускаемся в узел.children[numKeys]
     */
    private BPlusTreeNode findLeaf(int key) {
        // TODO: implement
    }

    public void printTree() {
        BPlusTreeNode root = pageCache.get(rootPageId);
        printNode(root, 0);
    }

    private void printNode(BPlusTreeNode node, int depth) {
        System.out.println("  ".repeat(depth) + node);

        if (!node.isLeaf()) {
            for (var childId : node.getChildPageIds()) {
                if (childId != null && (Integer) childId >= 0) {
                    BPlusTreeNode child = pageCache.get(childId);
                    if (child != null) {
                        printNode(child, depth + 1);
                    }
                }
            }
        }
    }
}

package cwts.networkanalysis;

import it.unimi.dsi.fastutil.ints.AbstractIntList;
import it.unimi.dsi.fastutil.ints.IntList;

public class GeertensIntList extends AbstractIntList {
    private Node head;
    private Node tail;
    private int size;

    public GeertensIntList(){

    }

    @Override
    public int getInt(int index){
        if(index >= size) return -2147483648;
        Node pointer = head;
        for(int i = 0; i < index; i++){
            pointer = pointer.getNextNode();
        }
        return pointer.getValue();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean add(int k){
        if(head == null){
            head = new Node(k, null, null);
        }
        else if(tail == null){
            tail = new Node(k, null, head);
            head.setNextNode(tail);
        }
        else {
            Node newNode = new Node(k,null, tail);
            tail.setNextNode(newNode);
            tail = newNode;
        }
        size++;
        return true;
    }

    @Override
    public boolean isEmpty(){
        if(head == null) return true;
        return false;
    }

    @Override
    public int popInt(){
        if(size == 0) return -2147483648;
        int value = head.getValue();
        if(size == 1){
            head = null;
        }
        else if(size == 2){
            head.setNextNode(null);
            tail = null;
        }
        else{
            Node newHead = head.getNextNode();
            head.setNextNode(null);
            head = newHead;
        }
        size--;
        return value;
    }

    public boolean addAll(GeertensIntList l){
        if(l.size() > 1){
            if(size == 0){
                head = l.getFirstInt();
            }
            else if(size == 1){
                head.setNextNode(l.getFirstInt());
            }
            else{
                tail.setNextNode(l.getFirstInt());
                l.getFirstInt().setPrevNode(tail);
            }
            tail = l.getLastInt();
        }
        else if(l.size == 1){
            if(size == 0){
                head = l.getFirstInt();
            }
            else if(size == 1){
                head.setNextNode(l.getFirstInt());
                tail = l.getFirstInt();
                tail.setPrevNode(head);
            }
            else{
                tail.setNextNode(l.getFirstInt());
                l.getFirstInt().setPrevNode(tail);
            }
        }
        size += l.size();

        return true;
    }

    public Node getFirstInt(){
        return head;
    }

    public Node getLastInt(){
        return tail;
    }

    public void printList(){
        Node pointer = head;
        while(pointer != null){
            System.out.println(pointer.getValue());
            pointer = pointer.getNextNode();
        }
    }
}

class Node {
    private int value;
    private Node nextNode;
    private Node prevNode;

    public Node(int value, Node nextNode, Node prevNode){
        this.value = value;
        this.nextNode = nextNode;
        this.prevNode = prevNode;
    }

    public void setValue(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }

    public void setNextNode(Node nextNode){
        this.nextNode = nextNode;
    }

    public Node getNextNode(){
        return nextNode;
    }

    public void setPrevNode(Node prevNode){
        this.prevNode = prevNode;
    }

    public Node getPrevNode(){
        return prevNode;
    }
}
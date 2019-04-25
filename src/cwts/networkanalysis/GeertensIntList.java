package cwts.networkanalysis;

public class GeertensIntList{
    private Node head;
    private Node tail;
    private int size;

    public GeertensIntList(){

    }

    public int size() {
        return size;
    }

    public void setSize(int size){
        this.size = size;
    }

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

    public boolean isEmpty(){
        if(head == null) return true;
        return false;
    }

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
                head = l.getHead();
            }
            else if(size == 1){
                head.setNextNode(l.getHead());
            }
            else{
                tail.setNextNode(l.getHead());
                l.getHead().setPrevNode(tail);
            }
            tail = l.getTail();
        }
        else if(l.size == 1){
            if(size == 0){
                head = l.getHead();
            }
            else {
                if(size == 1){
                    head.setNextNode(l.getHead());
                }
                else{
                    tail.setNextNode(l.getHead());
                }
                tail = l.getHead();
                tail.setPrevNode(head);
            }
        }

        size += l.size();

        return true;
    }

    public Node getHead(){
        return head;
    }

    public void setHead(Node head){
        this.head = head;
    }

    public Node getTail(){
        return tail;
    }

    public void setTail(Node tail){
        this.tail = tail;
    }

    public GeertensIntList popSubList(int fraction){
        GeertensIntList subList = new GeertensIntList();
        int newSize = (size+fraction-1) / fraction;
        if(newSize < 10 || fraction == 1){
            subList.setSize(size);
            subList.setHead(head);
            subList.setTail(tail);
            size = 0;
            head = null;
            tail = null;
        }
        else {
            subList.setSize(newSize);
            subList.setHead(head);
            Node pointer = head;
            for(int i = 1; i < newSize; i++){
                pointer = pointer.getNextNode();
            }
            subList.setTail(pointer);
            head = pointer.getNextNode();
            pointer.setNextNode(null);
            head.setPrevNode(null);
            size -= newSize;
        }
        return subList;
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
}
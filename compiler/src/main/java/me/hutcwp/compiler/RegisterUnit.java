package me.hutcwp.compiler;

import java.util.ArrayList;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public class RegisterUnit {

    private Element mManagerElement;

    private Element parent;

    private TypeMirror valueType;

    private ArrayList<Element> children = new ArrayList<>();

    public ArrayList<Element> getChildren() {
        return children;
    }

    public void addChild(Element child) {
        if (parent == null || checkChildType(child)) {
            System.out.println("checkChildType(child) = " + checkChildType(child));
            children.add(child);
        } else {
            throw new IllegalArgumentException(
                    String.format("child value type not match!!! child is %s ,but parent is %s",
                            child.asType().toString(), parent.asType().toString()));
        }
    }

    public Element getParent() {
        return parent;
    }

    public void setParent(Element parent) {
        this.parent = parent;
        this.valueType = parent.asType();
        if (!children.isEmpty()) {
            for (Element child : children) {
                if (!checkChildType(child)) {
                    throw new IllegalArgumentException("setParent() error, value type not match!!!");
                }
            }
        }
    }

    // 校验类型
    private boolean checkChildType(Element child) {
        return (((TypeElement) child).getSuperclass() == parent.asType());
    }

    public Element getManagerElement() {
        return mManagerElement;
    }

    public void setManagerElement(Element managerElement) {
        mManagerElement = managerElement;
    }

    public TypeMirror getValueType() {
        return valueType;
    }

    public void setValueType(TypeMirror valueType) {
        this.valueType = valueType;
    }

    @Override
    public String toString() {
        return "RegisterUnit{" +
                "mManagerElement=" + mManagerElement +
                ", parent=" + parent +
                ", valueType=" + valueType +
                ", children=" + children +
                '}';
    }
}

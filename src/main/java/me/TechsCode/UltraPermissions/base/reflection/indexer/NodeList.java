package me.TechsCode.UltraPermissions.base.reflection.indexer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class NodeList extends ArrayList<Node> {
   public NodeList(int var1) {
      super(var1);
   }

   public NodeList() {
      super();
   }

   public NodeList(Collection<? extends Node> var1) {
      super(var1);
   }

   public NodeList classes() {
      return this.stream().filter(Node::isClass).collect(Collectors.toCollection(NodeList::new));
   }
}

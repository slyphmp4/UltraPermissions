package me.techscode.security;

import java.lang.reflect.*;
import java.net.InetAddress;
import java.util.*;
import java.security.Permission;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerLoginEvent;
import me.TechsCode.UltraPermissions.*;
import me.TechsCode.UltraPermissions.base.command.*;
import me.TechsCode.UltraPermissions.base.mysql.*;
import me.TechsCode.UltraPermissions.base.storage.*;
import me.TechsCode.UltraPermissions.base.storage.syncing.SyncingAgent;
import me.TechsCode.UltraPermissions.commands.*;
import me.TechsCode.UltraPermissions.storage.collection.*;
import me.TechsCode.UltraPermissions.storage.objects.*;
import me.TechsCode.UltraPermissions.dependencies.gson.JsonObject;
import sun.misc.Unsafe;

/** Negative regressions from the 5.6.14 audit, using the production command/event/storage paths. */
final class AuditRegressionChecks {
  static Unsafe unsafe;
  static Object defaultValue(Class<?> c) {
    if(c==boolean.class)return false;if(c==int.class)return 0;if(c==long.class)return 0L;
    if(c==double.class)return 0d;if(c==float.class)return 0f;if(c==byte.class)return (byte)0;
    if(c==short.class)return (short)0;if(c==char.class)return '\0';return null;
  }
  static Field field(Object o,String n)throws Exception {
    for(Class<?> c=o.getClass();c!=null;c=c.getSuperclass())try{Field f=c.getDeclaredField(n);f.setAccessible(true);return f;}catch(NoSuchFieldException e){}
    throw new NoSuchFieldException(n);
  }
  static void set(Object o,String n,Object v)throws Exception{field(o,n).set(o,v);}
  static <T>T alloc(Class<T> c)throws Exception{return c.cast(unsafe.allocateInstance(c));}
  static Player player(UUID uuid,String name) {
    return (Player)Proxy.newProxyInstance(Player.class.getClassLoader(),new Class[]{Player.class},(o,m,a)->switch(m.getName()){
      case "getUniqueId"->uuid;case "getName"->name;case "hashCode"->uuid.hashCode();
      case "equals"->a[0] instanceof org.bukkit.OfflinePlayer other && uuid.equals(other.getUniqueId());case "hasPermission"->true;default->defaultValue(m.getReturnType());});
  }
  public static class FixturePlugin extends UltraPermissions {
    UserList users;GroupPathList paths;List<String> logs;
    me.TechsCode.UltraPermissions.base.AppearanceRegistry appearance;
    public FixturePlugin(){super(null);}
    public UserList getUsers(){return users;}
    public GroupPathList getGroupPaths(){return paths;}
    public boolean isDefaultPermissionsEnabled(){return false;}
    public me.TechsCode.UltraPermissions.base.AppearanceRegistry getAppearanceRegistry(){return appearance;}
    public DefaultGroupAssignOption getDefaultGroupAssignOption(){return DefaultGroupAssignOption.FIRST_JOIN;}
    public User registerUser(UUID id,String name,boolean defaults){User u=new User(id,name,false,new HashMap<>(),"","",null);users.add(u);return u;}
    public Optional<me.TechsCode.UltraPermissions.base.networking.NServer> getThisServer(){return Optional.empty();}
    public me.TechsCode.UltraPermissions.base.debug.DebugRegistery getDebugRegistery(){return new me.TechsCode.UltraPermissions.base.debug.DebugRegistery();}
    public String getPrefix(){return "";}
    public String getName(){return "UltraPermissions";}
    public void log(String s){logs.add(s);}
  }
  public static class FixtureGroup extends Group {
    Stored<Group> reference;
    FixtureGroup(String id,String name){super(id,name,null,null,0,new HashSet<>(),false,null,null,null,new HashMap<>());reference=new Stored<Group>(id,null){public Optional<Group> get(){return Optional.of(FixtureGroup.this);}};}
    public Stored<Group> toStored(){return reference;}
  }
  public static class AuditItem extends Storable {
    JsonObject data;
    public String getKey(){return "fixture";}public void setKey(String s){}
    public JsonObject getState(){return data;}public void setState(JsonObject j,me.TechsCode.UltraPermissions.base.TechPlugin<?> p){data=j;}
    public void onMount(me.TechsCode.UltraPermissions.base.TechPlugin<?> p){}
    public void persist(){sync();}
  }
  public static class AuditStorage extends Storage<AuditItem> {
    public AuditStorage(){super(null,null,null,null,false);}
    public void onMount(AuditItem x){}public void onCreation(AuditItem x){}public void onChange(AuditItem a,AuditItem b){}
    public void onDestroy(AuditItem x){}public void onDataSynchronization(){}
  }
  public static class Backend extends StorageImplementation {
    int writes;
    Backend(){super(null,"fixture",false);}
    public void update(String k,JsonObject j,WriteCallback c){writes++;c.onSuccess();}
    public void create(String k,JsonObject j,WriteCallback c){update(k,j,c);}
    public void destroy(String k,WriteCallback c){c.onSuccess();}
    public void read(String k,ReadCallback c){c.onSuccess(new HashMap<>());}
  }
  static void execute(PromoteCommand cmd,FixturePlugin plugin,CommandSender sender,String who,String path) {
    var root=CommandNode.create("promote");cmd.define(sender,root);
    var args=new Arguments();args.addAll(List.of("promote",who,path));root.execute(sender,null,args,x->{});
  }
  static void run()throws Exception {
    Field u=Unsafe.class.getDeclaredField("theUnsafe");u.setAccessible(true);unsafe=(Unsafe)u.get(null);
    Server prior=Bukkit.getServer();
    PluginManager manager=(PluginManager)Proxy.newProxyInstance(PluginManager.class.getClassLoader(),new Class[]{PluginManager.class},(o,m,a)->m.getReturnType()==Set.class?Set.of():defaultValue(m.getReturnType()));
    Server wrapper=(Server)Proxy.newProxyInstance(Server.class.getClassLoader(),new Class[]{Server.class},(o,m,a)->m.getName().equals("getPluginManager")?manager:m.invoke(prior,a));
    Field server=Bukkit.class.getDeclaredField("server");server.setAccessible(true);server.set(null,wrapper);
    FixturePlugin plugin=alloc(FixturePlugin.class);plugin.users=new UserList();plugin.paths=new GroupPathList();plugin.logs=new ArrayList<>();
    Field instance=me.TechsCode.UltraPermissions.base.TechPlugin.class.getDeclaredField("instance");instance.setAccessible(true);Object previousInstance=instance.get(null);instance.set(null,plugin);
    try {
    plugin.appearance=new me.TechsCode.UltraPermissions.base.AppearanceRegistry(plugin);set(plugin.appearance,"updaterNotificationsEnabled",false);
    UUID id=UUID.randomUUID();User user=new User(id,"LoginFixture",false,new HashMap<>(),"","",null);plugin.users.add(user);
    PluginEvents events=alloc(PluginEvents.class);set(events,"plugin",plugin);set(events,"permissibleHashMap",new HashMap<>());
    for(int i=0;i<200;i++)events.login(new PlayerLoginEvent(player(id,"LoginFixture"),"fixture",InetAddress.getLoopbackAddress(),PlayerLoginEvent.Result.KICK_OTHER,"denied",InetAddress.getLoopbackAddress()));
    if(events.getPermissibles().size()!=0)throw new AssertionError("denied login retained player");
    for(int i=1;i<200;i++){
      UUID distinct=UUID.randomUUID();String name="Denied"+i;plugin.users.add(new User(distinct,name,false,new HashMap<>(),"","",null));
      events.login(new PlayerLoginEvent(player(distinct,name),"fixture",InetAddress.getLoopbackAddress(),PlayerLoginEvent.Result.KICK_OTHER,"denied",InetAddress.getLoopbackAddress()));
    }
    if(events.getPermissibles().size()!=0)throw new AssertionError("distinct denied logins retained players");
    int before=plugin.users.size();
    Player pending=player(UUID.randomUUID(),"Pending");
    events.login(new PlayerLoginEvent(pending,"fixture",InetAddress.getLoopbackAddress()));
    if(events.getPermissibles().size()!=0||plugin.users.size()!=before)throw new AssertionError("pre-join retained/persisted account");
    events.join(new org.bukkit.event.player.PlayerJoinEvent(pending,"joined"));
    if(events.getPermissibles().size()!=1||plugin.users.size()!=before+1)throw new AssertionError("successful join missing account/permissions");
    events.quit(new org.bukkit.event.player.PlayerQuitEvent(pending,"quit"));
    if(events.getPermissibles().size()!=0)throw new AssertionError("quit retained player");

    FixtureGroup low=new FixtureGroup("1","Low"),high=new FixtureGroup("2","High"),other=new FixtureGroup("3","OtherPathEntry");
    User alice=new User(UUID.randomUUID(),"Alice",false,new HashMap<>(),"","",null);alice.addGroup(low);
    User bob=new User(UUID.randomUUID(),"Bob",false,new HashMap<>(),"","",null);bob.addGroup(low);
    plugin.users.add(alice);plugin.users.add(bob);
    GroupPath pathA=alloc(GroupPath.class);set(pathA,"name","PathA");set(pathA,"groupPath",new ArrayList<>(List.of(low,high)));
    GroupPath pathB=alloc(GroupPath.class);set(pathB,"name","PathB");set(pathB,"groupPath",new ArrayList<>(List.of(other)));
    plugin.paths.add(pathA);plugin.paths.add(pathB);
    CommandSender sender=(CommandSender)Proxy.newProxyInstance(CommandSender.class.getClassLoader(),new Class[]{CommandSender.class},(o,m,a)->m.getName().equals("hasPermission")?true:defaultValue(m.getReturnType()));
    PromoteCommand promote=alloc(PromoteCommand.class);set(promote,"plugin",plugin);
    execute(promote,plugin,sender,"Alice","PathA");
    if(!alice.getGroups().contains(high.toStored()))throw new AssertionError("baseline promotion failed");
    execute(promote,plugin,sender,"Bob","PathB");
    if(bob.getGroups().contains(other.toStored())||!bob.getGroups().contains(low.toStored()))throw new AssertionError("cross-command rank leaked");
    DemoteCommand demote=alloc(DemoteCommand.class);set(demote,"plugin",plugin);
    for(String[] call:List.of(new String[]{"Alice","PathA"},new String[]{"Bob","PathB"})){
      var root=CommandNode.create("demote");demote.define(sender,root);var args=new Arguments();args.addAll(List.of("demote",call[0],call[1]));root.execute(sender,null,args,x->{});
    }
    if(!alice.getGroups().contains(low.toStored())||bob.getGroups().contains(other.toStored()))throw new AssertionError("demote state leaked");

    AuditStorage storage=alloc(AuditStorage.class);Backend backend=new Backend();
    set(storage,"plugin",plugin);set(storage,"storable",AuditItem.class);set(storage,"implementation",backend);set(storage,"syncingAgent",new SyncingAgent.EmptyAgent());
    AuditItem item=new AuditItem();item.data=new JsonObject();item.data.addProperty("prefix","Admin");item.setLastSyncedState(item.data);item.setStorage(storage);
    item.data.addProperty("prefix","admin");item.persist();int caseWrites=backend.writes;
    item.data.addProperty("prefix","Moderator");item.persist();
    if(caseWrites!=1||backend.writes!=2)throw new AssertionError("case-only update was not saved");


    MySQLRegistry registry=new MySQLRegistry();JsonObject config=new JsonObject();
    config.addProperty("hostname","db.invalid");config.addProperty("port","3306");config.addProperty("database","fixture");config.addProperty("username","fixture");config.addProperty("password","fixture");config.addProperty("ssl",false);registry.setState(config);
    MySQLManager mysql=alloc(MySQLManager.class);set(mysql,"plugin",plugin);set(mysql,"registry",registry);
    try{mysql.setup();throw new AssertionError("configured MySQL failure silently accepted");}catch(StorageStartupException expected){}
    if(!registry.hasCredentials()||mysql.isEnabled())throw new AssertionError("fallback probe changed");
    if(!StorageStartupException.isStorageFailure(new IllegalStateException(new java.lang.reflect.InvocationTargetException(new StorageStartupException("fixture")))))throw new AssertionError("wrapped startup failure lost");
    if(StorageStartupException.isStorageFailure(new IllegalStateException("unrelated")))throw new AssertionError("unrelated failure misclassified");
    System.out.println("Audit regressions passed: rank paths, case-sensitive persistence, denied/pending/joined/quit sessions and configured MySQL fail-closed.");
    } finally {server.set(null,prior);instance.set(null,previousInstance);}
  }
}

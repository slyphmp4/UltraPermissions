package me.techscode.security;

import java.lang.reflect.*;
import java.net.InetAddress;
import java.util.*;
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
  private static int assertions;
  private static void check(boolean condition,String message){assertions++;if(!condition)throw new AssertionError(message);}
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
    MySQLManager manager;
    public FixturePlugin(){super(null);}
    public UserList getUsers(){return users;}
    public MySQLManager getMySQLManager(){return manager;}
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
  public static class FixtureManager extends MySQLManager {
    java.sql.Connection connection;
    public FixtureManager(){super(null);}
    public java.sql.Connection getConnection(){return connection;}
  }
  static void storageReadLimits(FixturePlugin plugin)throws Exception {
    var config=alloc(SecurityChecks.TestFileConfiguration.class);config.values=new HashMap<>();
    config.values.put("good","{\"prefix\":\"admin\"}");config.values.put("unrelated-corrupt","{");
    var local=alloc(me.TechsCode.UltraPermissions.base.storage.implementations.LocalFile.class);
    set(local,"configuration",config);
    final int[] count={-1};
    ReadCallback callback=new ReadCallback(){public void onSuccess(HashMap<String,JsonObject> rows){count[0]=rows.size();}public void onFailure(Exception e){throw new AssertionError(e);}};
    local.read("good",callback);check(count[0]==1,"single-key read parsed unrelated records");
    local.read("missing",callback);check(count[0]==0,"missing local key returned unrelated rows");
    try{local.read("*",callback);throw new AssertionError("full malformed storage was accepted");}catch(IllegalStateException expected){assertions++;}
    byte[] value=("{\"name\":\""+"x".repeat(4*1024*1024-32)+"\"}").getBytes(java.nio.charset.StandardCharsets.UTF_8);
    int[] rows={0},closed={0},fetch={0};
    var result=(java.sql.ResultSet)Proxy.newProxyInstance(java.sql.ResultSet.class.getClassLoader(),new Class[]{java.sql.ResultSet.class},(o,m,a)->switch(m.getName()){
      case "next"->++rows[0]<=20;case "getBytes"->value;case "getString"->"row"+rows[0];case "close"->{closed[0]++;yield null;}default->defaultValue(m.getReturnType());});
    var statement=(java.sql.PreparedStatement)Proxy.newProxyInstance(java.sql.PreparedStatement.class.getClassLoader(),new Class[]{java.sql.PreparedStatement.class},(o,m,a)->switch(m.getName()){
      case "executeQuery"->result;case "setFetchSize"->{fetch[0]=(Integer)a[0];yield null;}case "close"->{closed[0]++;yield null;}default->defaultValue(m.getReturnType());});
    var connection=(java.sql.Connection)Proxy.newProxyInstance(java.sql.Connection.class.getClassLoader(),new Class[]{java.sql.Connection.class},(o,m,a)->switch(m.getName()){
      case "prepareStatement"->{check(a.length==3 && (Integer)a[1]==java.sql.ResultSet.TYPE_FORWARD_ONLY && (Integer)a[2]==java.sql.ResultSet.CONCUR_READ_ONLY,"query is not forward-only/read-only");yield statement;}
      case "close"->{closed[0]++;yield null;}default->defaultValue(m.getReturnType());});
    FixtureManager manager=alloc(FixtureManager.class);manager.connection=connection;plugin.manager=manager;
    var mysql=alloc(me.TechsCode.UltraPermissions.base.storage.implementations.MySQL.class);set(mysql,"plugin",plugin);set(mysql,"tableName","UltraPermissions_Fixture");
    var failure=new java.util.concurrent.atomic.AtomicReference<Exception>();
    mysql.read("*",new ReadCallback(){public void onSuccess(HashMap<String,JsonObject> values){throw new AssertionError("oversized database accepted");}public void onFailure(Exception e){failure.set(e);}});
    check(failure.get()!=null && failure.get().getMessage().contains("aggregate byte limit"),"aggregate database size was not bounded");
    check(rows[0]==17 && closed[0]==3 && fetch[0]==Integer.MIN_VALUE,"streaming read failed to stop/close at aggregate limit");
  }
  static void hikariLifecycle()throws Exception {
    var closed=new java.util.concurrent.atomic.AtomicInteger();
    java.sql.Driver driver=new java.sql.Driver(){
      public boolean acceptsURL(String url){return url.startsWith("jdbc:mysql://localhost:3306/hikari_fixture?");}
      public java.sql.Connection connect(String url,Properties properties){
        if(!acceptsURL(url))return null;
        return (java.sql.Connection)Proxy.newProxyInstance(java.sql.Connection.class.getClassLoader(),new Class[]{java.sql.Connection.class},(o,m,a)->switch(m.getName()){
          case "isValid","getAutoCommit"->true;case "getTransactionIsolation"->java.sql.Connection.TRANSACTION_READ_COMMITTED;
          case "close"->{closed.incrementAndGet();yield null;}default->defaultValue(m.getReturnType());});
      }
      public java.sql.DriverPropertyInfo[] getPropertyInfo(String url,Properties info){return new java.sql.DriverPropertyInfo[0];}
      public int getMajorVersion(){return 1;}public int getMinorVersion(){return 0;}public boolean jdbcCompliant(){return true;}
      public java.util.logging.Logger getParentLogger(){return java.util.logging.Logger.getLogger("AuditJdbcFixture");}
    };
    java.sql.DriverManager.registerDriver(driver);
    ConnectionFactory pool=null;
    try {
      pool=new ConnectionFactory(new MySQLCredentials("localhost","3306","hikari_fixture","fixture","fixture"),false,0,1);
      check(pool.testConnection().isValid(),"refreshed Hikari could not acquire/return a JDBC fixture connection");
      try(var connection=pool.newConnection()){check(connection!=null && connection.isValid(1),"refreshed Hikari connection proxy failed");}
    } finally {
      if(pool!=null)pool.close();
      java.sql.DriverManager.deregisterDriver(driver);
    }
    check(closed.get()>0,"Hikari shutdown left its physical connection open");
  }
  static int run()throws Exception {
    assertions=0;
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
    check(events.getPermissibles().isEmpty(),"denied login retained player");
    for(int i=1;i<200;i++){
      UUID distinct=UUID.randomUUID();String name="Denied"+i;plugin.users.add(new User(distinct,name,false,new HashMap<>(),"","",null));
      events.login(new PlayerLoginEvent(player(distinct,name),"fixture",InetAddress.getLoopbackAddress(),PlayerLoginEvent.Result.KICK_OTHER,"denied",InetAddress.getLoopbackAddress()));
    }
    check(events.getPermissibles().isEmpty(),"distinct denied logins retained players");
    int before=plugin.users.size();
    Player pending=player(UUID.randomUUID(),"Pending");
    events.login(new PlayerLoginEvent(pending,"fixture",InetAddress.getLoopbackAddress()));
    check(events.getPermissibles().isEmpty() && plugin.users.size()==before,"pre-join retained/persisted account");
    events.join(new org.bukkit.event.player.PlayerJoinEvent(pending,"joined"));
    check(events.getPermissibles().size()==1 && plugin.users.size()==before+1,"successful join missing account/permissions");
    events.quit(new org.bukkit.event.player.PlayerQuitEvent(pending,"quit"));
    check(events.getPermissibles().isEmpty(),"quit retained player");

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
    check(alice.getGroups().contains(high.toStored()),"baseline promotion failed");
    execute(promote,plugin,sender,"Bob","PathB");
    check(!bob.getGroups().contains(other.toStored()) && bob.getGroups().contains(low.toStored()),"cross-command rank leaked");
    DemoteCommand demote=alloc(DemoteCommand.class);set(demote,"plugin",plugin);
    for(String[] call:List.of(new String[]{"Alice","PathA"},new String[]{"Bob","PathB"})){
      var root=CommandNode.create("demote");demote.define(sender,root);var args=new Arguments();args.addAll(List.of("demote",call[0],call[1]));root.execute(sender,null,args,x->{});
    }
    check(alice.getGroups().contains(low.toStored()) && !bob.getGroups().contains(other.toStored()),"demote state leaked");

    AuditStorage storage=alloc(AuditStorage.class);Backend backend=new Backend();
    set(storage,"plugin",plugin);set(storage,"storable",AuditItem.class);set(storage,"implementation",backend);set(storage,"syncingAgent",new SyncingAgent.EmptyAgent());
    AuditItem item=new AuditItem();item.data=new JsonObject();item.data.addProperty("prefix","Admin");item.setLastSyncedState(item.data);item.setStorage(storage);
    item.data.addProperty("prefix","admin");item.persist();int caseWrites=backend.writes;
    item.data.addProperty("prefix","Moderator");item.persist();
    check(caseWrites==1 && backend.writes==2,"case-only update was not saved");
    storageReadLimits(plugin);
    hikariLifecycle();

    MySQLRegistry registry=new MySQLRegistry();JsonObject config=new JsonObject();
    config.addProperty("hostname","db.invalid");config.addProperty("port","3306");config.addProperty("database","fixture");config.addProperty("username","fixture");config.addProperty("password","fixture");config.addProperty("ssl",false);registry.setState(config);
    MySQLManager mysql=alloc(MySQLManager.class);set(mysql,"plugin",plugin);set(mysql,"registry",registry);
    try{mysql.setup();throw new AssertionError("configured MySQL failure silently accepted");}catch(StorageStartupException expected){}
    check(registry.hasCredentials() && !mysql.isEnabled(),"fallback probe changed");
    check(StorageStartupException.isStorageFailure(new IllegalStateException(new java.lang.reflect.InvocationTargetException(new StorageStartupException("fixture")))),"wrapped startup failure lost");
    check(!StorageStartupException.isStorageFailure(new IllegalStateException("unrelated")),"unrelated failure misclassified");
    System.out.println("Audit regressions passed: rank paths, case-sensitive persistence, denied/pending/joined/quit sessions and configured MySQL fail-closed.");
    return assertions;
    } finally {server.set(null,prior);instance.set(null,previousInstance);}
  }
}

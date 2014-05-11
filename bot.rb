require 'sequel'
require 'mysql2'
require 'digest/sha1'
require 'pry'
require 'logger'
require 'cinch'
require 'json'
require 'redis'
require 'redis/objects'
require 'ohm'

Redis.current = Redis.new

class CommandInterfaceReply
  def initialize(bot)
    @redis = Redis.new
    @bot = bot
  end

  def start
    while true
      message = @redis.blpop("command_outgoing");
      @bot.handlers.dispatch(:command_outgoing, nil, message);
    end
  end
end

class Question
  include Redis::Objects
  
  def initialize(reset=false,question="")
    if reset
      self.question = question
      self.voters.clear
      self.for.reset
      self.against.reset
    end
  end

  def id
    1
  end

  value :question
  list :voters
  counter :for
  counter :against

  def add_vote(user, vote)
    if self.voters.values.include? user
      return "#{user} has already voted."
    end    
    case vote.downcase
    when /^y/
      self.for.increment
      self.voters << user
      return "Vote recorded for #{user}"
    when /^n/
      self.against.increment
      self.voters << user
      return "Vote recorded for #{user}"
    else
      # do nothing
    end
  end

  def results
    yay = self.for.value
    nay = self.against.value
    return "[ #{yay}Y / #{nay}N ] #{self.question.value}"
  end
end

DB = Sequel.connect('mysql2://root:this%20fucking%20game%20sucks@localhost:3306/openrscdv25')

BASE = {
  "user" => "828804",
  "username" => "",
  "group_id" => 0,
  "owner" => 20,
  "owner_username" => nil,
  "sub_expires" => 0,
  "combat" => 3,
  "skill_total" => 3,
  "x" => 131,
  "y" => 508,
  "fatigue" => 0,
  "combatstyle" => 0,
  "block_chat" => 0,
  "block_private" => 0,
  "block_trade" => 0,
  "block_duel" => 0,
  "cameraauto" => 0,
  "onemouse" => 0,
  "soundoff" => 0,
  "showroof" => 0,
  "autoscreenshot" => 0,
  "combatwindow" => 0,
  "haircolour" => 2,
  "topcolour" => 8,
  "trousercolour" => 14,
  "skincolour" => 0,
  "headsprite" => 1,
  "bodysprite" => 2,
  "male" => 1,
  "skulled" => 0,
  "pass" => nil,
  "creation_date" => nil,
  "creation_ip" => "127.0.0.1",
  "login_date" => 0,
  "login_ip" => "0.0.0.0",
  "playermod" => 0,
  "loggedin" => 0,
  "banned" => 0,
  "muted" => 0,
  "deaths" => 0,
  "online" => 0,
  "world" => 1,
  "quest_points" => nil
}

module RSC
  def self.u2h(string)
    base = string.dup
    base.downcase!
    new_base = ""
    i = 0
    base.length.times do
      c = base[i]
      if (c >= 'a' && c <= 'z')
        new_base += c
      elsif (c >= '0' and c <= '9')
        new_base += c
      else
        new_base += ' '
      end
      i += 1
    end
    new_base.strip!
    if new_base.length > 12
      new_base = new_base[0..12]
    end
    int = 0
    i = 0
    new_base.length.times do 
      c = new_base[i]
      int *= 37
      if (c >= 'a' && c <= 'z')
        int += ((1 + c.bytes[0]) - 97)
      elsif (c >= '0' && c <= '9')
        int += ((27 + c.bytes[0]) - 48)
      end
      i += 1
    end
    return int
  end
end

class Player < Sequel::Model(:rsca2_players)
  set_primary_key :id

  def u2h(string)
    base = string.dup
    base.downcase!
    new_base = ""
    i = 0
    base.length.times do
      c = base[i]
      if (c >= 'a' && c <= 'z')
        new_base += c
      elsif (c >= '0' and c <= '9')
        new_base += c
      else
        new_base += ' '
      end
      i += 1
    end
    new_base.strip!
    if new_base.length > 12
      new_base = new_base[0..12]
    end
    int = 0
    i = 0
    new_base.length.times do 
      c = new_base[i]
      int *= 37
      if (c >= 'a' && c <= 'z')
        int += ((1 + c.bytes[0]) - 97)
      elsif (c >= '0' && c <= '9')
        int += ((27 + c.bytes[0]) - 48)
      end
      i += 1
    end
    return int
  end

  def setup(user, owner, password)
    self.values.merge!(BASE.dup)
    self.values["user"] = RSC.u2h(user)
    self.values["username"] = user
    self.values["pass"] = ::Digest::SHA1.hexdigest(password)
    self.values["creation_date"] = Time.now
    self.values["owner_username"] = owner
  end

  def finalize_new_user
    user = self.values[:user]
    begin
      DB.transaction do
        DB[:rsca2_curstats].insert({
          user: user,
          cur_attack: 1,
          cur_defense: 1,
          cur_strength: 1,
          cur_hits: 10,
          cur_ranged: 1,
          cur_prayer: 1,
          cur_magic: 1,
          cur_cooking: 1,
          cur_woodcut: 1,
          cur_fletching: 1,
          cur_fishing: 1,
          cur_firemaking: 1,
          cur_crafting: 1,
          cur_smithing: 1,
          cur_mining: 1,
          cur_herblaw: 1,
          cur_agility: 1,
          cur_thieving: 1,
        })
      end
      puts 1
      DB.transaction do
        DB[:rsca2_experience].insert({
          user: user,
          exp_attack: 0,
          exp_defense: 0,
          exp_strength: 0,
          exp_hits: 1200,
          exp_ranged: 0,
          exp_prayer: 0,
          exp_magic: 0,
          exp_cooking: 0,
          exp_woodcut: 0,
          exp_fletching: 0,
          exp_fishing: 0,
          exp_firemaking: 0,
          exp_crafting: 0,
          exp_smithing: 0,
          exp_mining: 0,
          exp_herblaw: 0,
          exp_agility: 0,
          exp_thieving: 0,
        })
      end
      puts 2
      DB.transaction do
        DB[:rsca2_invitems].insert({
          user: user,
          id: 1263,
          amount: 1,
          wielded: 0,
          slot: 1
        })
      end
      puts 3
      DB.transaction do
        DB[:rsca2_invitems].insert({
          user: user,
          id: 10,
          amount: 100000,
          wielded: 0,
          slot: 0
        })
      end
      puts 4
      DB.transaction do
        DB[:rsca2_invitems].insert({
          user: user,
          id: 77,
          amount: 1,
          wielded: 0,
          slot: 2
        })
      end
      puts 5
    rescue Exception => e
      return "Failed to save user: #{e.message}"
    end
    return "User saved successfully."
  end
end

if ARGV[0] == 'cli'
  binding.pry
  exit 0
end

class CommandInterfaceReply
  def initialize(bot)
    @redis = Redis.new
    @bot = bot
  end

  def start
    while true
      message = @redis.blpop("command_outgoing");
      @bot.handlers.dispatch(:command_outgoing, nil, message);
    end
  end
end

class CommandInterface
  include Cinch::Plugin

  def initialize(*args)
    super
    @redis = Redis.new
    @reply = Thread.new { CommandInterfaceReply.new(@bot).start }
    @chan = "#clubhouse"
    @admins = [ 'gen0cide_' ]
  end

  match /server (.+)$/, method: :server_command

  listen_to :command_outgoing, method: :command_reply

  def server_command(m, command)
    return unless check_channel m.channel
    @redis.rpush("command_incoming", command)
  end

  def check_channel(channel)
    @chan == channel
  end

  def is_admin?(user)
    @admins.include? user.nick
  end

  def command_reply(m, reply)
    Channel(@chan).send(reply[1])
  end
end

bot = Cinch::Bot.new do
  configure do |c|
    c.nick            = 'NSA'
    c.user            = 'NSA'
    c.realname        = 'NSA'
    c.server          = 'irc.clubhouse.ws'
    c.port            = 6697
    c.ssl.use         = true
    c.channels        = ['#clubhouse']
    c.verbose         = true
    c.plugins.plugins = [CommandInterface]
  end

  on :message, /^!newvote (.+)$/ do |m,question|
    if ['JinnX','gen0cide_'].include? m.user.nick
      a = Question.new(true,question)
      m.reply a.results
    else
      m.reply "You are not an admin for voting."
    end
  end

  on :message, /^!vote (.+)$/ do |m,vote|
    a = Question.new
    resp = a.add_vote(m.user.nick,vote)
    m.reply(resp)
  end

  on :message, /^!curvote/ do |m|
    a = Question.new
    m.reply(a.results)
  end

  on :message, /^!rsc users/ do |m|
    a = Player.all
    a.each do |a|
      m.reply "#{a.values[:username]} (#{a.values[:owner_username]})"
      sleep 0.5
    end
  end

  on :message, /^!rsc create (.+) (.+)$/ do |m, username, password|
    username.gsub!(/[^A-Za-z0-9]/,'')
    if Player.filter(user: RSC.u2h(username)).all.length == 0
      new_player = Player.new
      new_player.setup(username, m.user.nick, password)
      new_player.save
      new_player.finalize_new_user      
      Channel("#clubhouse").send "Character #{username} has been created for #{m.user.nick}"  
      m.reply("Character #{username} has been created successfully.")
    else
      m.reply("There is already a user by that name.")
    end    
  end

  on :message, /^!help/ do |m|
    m.reply "AVAILABLE COMMANDS: !rsc users *** !rsc create USERNAME PASSWORD"
  end
end


bot.start
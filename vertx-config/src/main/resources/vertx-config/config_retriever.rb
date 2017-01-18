require 'vertx-config/config_stream'
require 'vertx/vertx'
require 'vertx/future'
require 'vertx/util/utils.rb'
# Generated from io.vertx.config.ConfigRetriever
module VertxConfig
  #  Defines a configuration retriever that read configuration from
  #  
  #  and tracks changes periodically.
  class ConfigRetriever
    # @private
    # @param j_del [::VertxConfig::ConfigRetriever] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxConfig::ConfigRetriever] the underlying java delegate
    def j_del
      @j_del
    end
    @@j_api_type = Object.new
    def @@j_api_type.accept?(obj)
      obj.class == ConfigRetriever
    end
    def @@j_api_type.wrap(obj)
      ConfigRetriever.new(obj)
    end
    def @@j_api_type.unwrap(obj)
      obj.j_del
    end
    def self.j_api_type
      @@j_api_type
    end
    def self.j_class
      Java::IoVertxConfig::ConfigRetriever.java_class
    end
    #  Creates an instance of the default implementation of the {::VertxConfig::ConfigRetriever}.
    # @param [::Vertx::Vertx] vertx the vert.x instance
    # @param [Hash] options the options, must not be <code>null</code>, must contain the list of configured store.
    # @return [::VertxConfig::ConfigRetriever] the created instance.
    def self.create(vertx=nil,options=nil)
      if vertx.class.method_defined?(:j_del) && !block_given? && options == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxConfig::ConfigRetriever.java_method(:create, [Java::IoVertxCore::Vertx.java_class]).call(vertx.j_del),::VertxConfig::ConfigRetriever)
      elsif vertx.class.method_defined?(:j_del) && options.class == Hash && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxConfig::ConfigRetriever.java_method(:create, [Java::IoVertxCore::Vertx.java_class,Java::IoVertxConfig::ConfigRetrieverOptions.java_class]).call(vertx.j_del,Java::IoVertxConfig::ConfigRetrieverOptions.new(::Vertx::Util::Utils.to_json_object(options))),::VertxConfig::ConfigRetriever)
      end
      raise ArgumentError, "Invalid arguments when calling create(#{vertx},#{options})"
    end
    #  Reads the configuration from the different 
    #  and computes the final configuration.
    # @yield handler receiving the computed configuration, or a failure if the configuration cannot be retrieved
    # @return [void]
    def get_config
      if block_given?
        return @j_del.java_method(:getConfig, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling get_config()"
    end
    #  Same as {::VertxConfig::ConfigRetriever#get_config}, but returning a  object. The result is a
    #  .
    # @return [::Vertx::Future]
    def get_config_future
      if !block_given?
        return ::Vertx::Util::Utils.safe_create(@j_del.java_method(:getConfigFuture, []).call(),::Vertx::Future, nil)
      end
      raise ArgumentError, "Invalid arguments when calling get_config_future()"
    end
    #  Closes the retriever.
    # @return [void]
    def close
      if !block_given?
        return @j_del.java_method(:close, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling close()"
    end
    #  Gets the last computed configuration.
    # @return [Hash{String => Object}] the last configuration
    def get_cached_config
      if !block_given?
        return @j_del.java_method(:getCachedConfig, []).call() != nil ? JSON.parse(@j_del.java_method(:getCachedConfig, []).call().encode) : nil
      end
      raise ArgumentError, "Invalid arguments when calling get_cached_config()"
    end
    #  Registers a listener receiving configuration changes. This method cannot only be called if
    #  the configuration is broadcasted.
    # @yield the listener
    # @return [void]
    def listen
      if block_given?
        return @j_del.java_method(:listen, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |event| yield(event != nil ? JSON.parse(event.toJson.encode) : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling listen()"
    end
    # @return [::VertxConfig::ConfigStream] the stream of configurations.
    def config_stream
      if !block_given?
        if @cached_config_stream != nil
          return @cached_config_stream
        end
        return @cached_config_stream = ::Vertx::Util::Utils.safe_create(@j_del.java_method(:configStream, []).call(),::VertxConfig::ConfigStream)
      end
      raise ArgumentError, "Invalid arguments when calling config_stream()"
    end
  end
end

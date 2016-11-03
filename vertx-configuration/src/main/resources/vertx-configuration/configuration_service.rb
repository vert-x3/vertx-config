require 'vertx/vertx'
require 'vertx-configuration/configuration_stream'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.configuration.ConfigurationService
module VertxConfiguration
  #  Defines a configuration service that read configuration from {Nil}
  #  and tracks changes periodically.
  class ConfigurationService
    # @private
    # @param j_del [::VertxConfiguration::ConfigurationService] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxConfiguration::ConfigurationService] the underlying java delegate
    def j_del
      @j_del
    end
    #  Creates an instance of the default implementation of the {::VertxConfiguration::ConfigurationService}.
    # @param [::Vertx::Vertx] vertx the vert.x instance
    # @param [Hash] options the options, must not be <code>null</code>, must contain the list of configured store.
    # @return [::VertxConfiguration::ConfigurationService] the created instance.
    def self.create(vertx=nil,options=nil)
      if vertx.class.method_defined?(:j_del) && !block_given? && options == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtConfiguration::ConfigurationService.java_method(:create, [Java::IoVertxCore::Vertx.java_class]).call(vertx.j_del),::VertxConfiguration::ConfigurationService)
      elsif vertx.class.method_defined?(:j_del) && options.class == Hash && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtConfiguration::ConfigurationService.java_method(:create, [Java::IoVertxCore::Vertx.java_class,Java::IoVertxExtConfiguration::ConfigurationServiceOptions.java_class]).call(vertx.j_del,Java::IoVertxExtConfiguration::ConfigurationServiceOptions.new(::Vertx::Util::Utils.to_json_object(options))),::VertxConfiguration::ConfigurationService)
      end
      raise ArgumentError, "Invalid arguments when calling create(vertx,options)"
    end
    #  Reads the configuration from the different {Nil}
    #  and computes the final configuration.
    # @yield handler receiving the computed configuration, or a failure if the configuration cannot be retrieved
    # @return [void]
    def get_configuration
      if block_given?
        return @j_del.java_method(:getConfiguration, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ar.result != nil ? JSON.parse(ar.result.encode) : nil : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling get_configuration()"
    end
    #  Closes the service.
    # @return [void]
    def close
      if !block_given?
        return @j_del.java_method(:close, []).call()
      end
      raise ArgumentError, "Invalid arguments when calling close()"
    end
    #  Gets the last computed configuration.
    # @return [Hash{String => Object}] the last configuration
    def get_cached_configuration
      if !block_given?
        return @j_del.java_method(:getCachedConfiguration, []).call() != nil ? JSON.parse(@j_del.java_method(:getCachedConfiguration, []).call().encode) : nil
      end
      raise ArgumentError, "Invalid arguments when calling get_cached_configuration()"
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
    #  @return the stream of configurations.
    # @return [::VertxConfiguration::ConfigurationStream]
    def configuration_stream
      if !block_given?
        if @cached_configuration_stream != nil
          return @cached_configuration_stream
        end
        return @cached_configuration_stream = ::Vertx::Util::Utils.safe_create(@j_del.java_method(:configurationStream, []).call(),::VertxConfiguration::ConfigurationStream)
      end
      raise ArgumentError, "Invalid arguments when calling configuration_stream()"
    end
  end
end

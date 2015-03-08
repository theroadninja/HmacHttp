#
# To get syntax highlighting in eclipse:
#
# http://www.eclipse.org/dltk/install.php
#
# http://download.eclipse.org/technology/dltk/updates/
#
require 'uri'
require 'net/http'
require 'securerandom'
require "openssl"
require "base64"
require 'optparse'


class SSRequest
  attr_accessor :requestId
	attr_accessor :stage
	attr_accessor :method
	attr_accessor :paramString
	attr_accessor :useHttps
	attr_accessor :protocolVersion
	attr_accessor :serviceName
	attr_accessor :key
	attr_accessor :extraSignedField
	attr_accessor :body
	
	#for debugging
	attr_accessor :fields
	
	def initialize()
    @requestId = SecureRandom.uuid;
    @stage = ""
    @method = ""
    @paramString = ""
		@protocolVersion = 0 #default for heroku
		@serviceName = "SkinCareSidekickApi" #default for heroku
		@key = nil
		@fields = {}
		@extraSignedField = ""
		@body = ""
	end
	
	
	#def toSignableFormat()
	#	return @requestId + ";" + @paramString + ";" + @method
	#end
	
	def createParamString()
	
	  
	  #old way, difficult to unit test:
		#parameters = ""
		#@fields.each {|k,v|
		#	#NOTE:  this will break if the values contain a '='
		#	parameters = parameters + "&" + URI.escape(k) + "=" + URI.escape(v.to_s)
		#}
		#@paramString = parameters
		
		sortedKeys = @fields.keys.sort
    parameters = ""
		sortedKeys.each{|k|
		  #cannot use standard escape methods, because of differences in how ruby/java does it
      #parameters = parameters + "&" + Hmac.urlEscape(k) + "=" + Hmac.urlEscape(@fields[k].to_s) 
		  parameters = parameters + "&" + k + "=" + @fields[k].to_s
		}
		@paramString = parameters
		
		
		
	end
	
end #class SSRequest


class Hmac
  
  # Escape a string the same way java's URLEncoder.encode() would.  See SimpleSignedRequest.getMethodParameterString()
  #
  #
  def self.urlEscape(s)
    return URI.escape(s).gsub(/\//, "%2F")
  end

  def self.calcSignatureByProtocol(request, skeyHex)
    
    case request.protocolVersion
    when "0"
      return calcSignatureProtocol0(request, skeyHex)
    when "1"
      return calcSignatureProtocol1(request, skeyHex)
    when "t2"
      return calcSignatureProtocolT2(request, skeyHex)
    when "t3"
      return calcSignatureProtocolT3(request, skeyHex)
    when "t4"
      return calcSignatureProtocolT4(request, skeyHex)
    else
      raise "unknown protocol: " + request.protocolVersion
    end
  end
  
  # Test Protocol 2: bare minimum, for troubleshooting signature bugs
  #
  #
  def self.calcSignatureProtocolT2(request, skeyHex)
    
    if request.protocolVersion.nil?
      raise "protocol version cannot be nil"
    else
      #raise request.protocolVersion
    end
    
    protocolVersion = (request.protocolVersion.nil?) ? "" : request.protocolVersion.chomp.downcase
    
    hmac = getHmac(keyFromHex(skeyHex),
      request.protocolVersion);
    
    return Base64.encode64(hmac).gsub!(/=+$/, "").chomp; #remove any trailing =
  end
  
  # Test Protocol 3: for troubleshooting signature bugs
  #
  #
  def self.calcSignatureProtocolT3(request, skeyHex)
    
    if request.protocolVersion.nil?
      raise "protocol version cannot be nil"
    else
      #raise request.protocolVersion
    end
    
    protocolVersion = (request.protocolVersion.nil?) ? "" : request.protocolVersion.chomp.downcase
    yyyymmdd = today();
    
    hmac = getHmac(keyFromHex(skeyHex),
      request.protocolVersion,
      yyyymmdd);
    
    return Base64.encode64(hmac).gsub!(/=+$/, "").chomp; #remove any trailing =
  end 
  
  # Test Protocol 4: for troubleshooting signature bugs
  #
  #
  def self.calcSignatureProtocolT4(request, skeyHex)
    
    if request.protocolVersion.nil?
      raise "protocol version cannot be nil"
    else
      #raise request.protocolVersion
    end
    
    protocolVersion = (request.protocolVersion.nil?) ? "" : request.protocolVersion.chomp.downcase
    yyyymmdd = today();
    
    hmac = getHmac(keyFromHex(skeyHex),
      request.protocolVersion,
      yyyymmdd,
      request.stage,
      request.serviceName);
    
    return Base64.encode64(hmac).gsub!(/=+$/, "").chomp; #remove any trailing =
  end 
  
  
  
  def self.calcProtocol0fragment(request)
    return request.requestId + ";" + request.paramString + ";" + request.method
  end
  
  # First signature protocol that actually hit production
  #
  #
  def self.calcSignatureProtocol0(request, skeyHex)
    request.createParamString()
    signableRequest = request.requestId + ";" + request.paramString + ";" + request.method
    yyyymmdd = today();
    
    hmac = getHmac(keyFromHex(skeyHex),
      yyyymmdd,
      request.stage,
      request.serviceName,
      signableRequest);
    
    return Base64.encode64(hmac).gsub!(/=+$/, "").chomp; #remove any trailing =
  end
  
  # Basic signature protocol for production
  #
  #
  def self.calcSignatureProtocol1(request, skeyHex)
    request.createParamString()
    signableRequest = request.requestId + ";" + request.paramString + ";" + request.method
    yyyymmdd = today();
    
    hmac = getHmac(keyFromHex(skeyHex),
      request.protocolVersion,
      request.requestId,
      yyyymmdd,
      request.serviceName,
      request.stage,
      request.method,
      request.paramString,
      request.extraSignedField,
      request.body);
    
    return Base64.encode64(hmac).gsub!(/=+$/, "").chomp; #remove any trailing =
  end
  
  
  # Turns a hex string into the key format we want
  # Params:
  # +sKeyHex the encryption key, as a UTF-8 string of hex characters
  def self.keyFromHex(sKeyHex)
    return [sKeyHex].pack("H*")
  end
  
  def self.getHmac(key, *message)
    

    #if sKeyHex.is_a?(String)
    #  sKeyHex = [sKeyHex]
    #end
    #
    #if sKeyHex.is_a?(Array)
    #  key = sKeyHex.pack("H*")
    #else
    #  raise "sKeyHex is wrong type"
    #end

    
    
    
    #http://stackoverflow.com/questions/9744392/how-to-get-ruby-generated-hmac-for-sha256-that-is-url-safe-to-match-java
    digest = OpenSSL::Digest::Digest.new("sha256")
    
    #hmac = OpenSSL::HMAC.digest(digest, key, message.encode('UTF-8'));
    
    message.each{|m|
      if m.nil? then
        m = ""
      end
      #hmac = OpenSSL::HMAC.digest(digest, key, m.encode('UTF-8'));
      key = OpenSSL::HMAC.digest(digest, key, m.encode('UTF-8'));
    }
    
    #return hmac;
    return key;

  end
  
  
  def self.createSignature(stage, serviceName, requestForSignature, key)

    
    
    
    yyyymmdd = today();
    
    key = getHmac(key.pack("H*"), yyyymmdd);
    #key = getHmac(key.pack("H*"), "test");
    #return key;
    
    key = getHmac(key, stage);
    
    key = getHmac(key, serviceName);
    
    
    
    hmac = getHmac(key, requestForSignature);
    #hmac = getHmac(key, "test");
    
    
    #note:  adding the .chomp to remove what appears to be a trailing newline, which
    #fucks up the http request by adding an extra newline before the next header, causing
    #the header section to be terminated early.
    return Base64.encode64(hmac).gsub!(/=+$/, "").chomp; #remove any trailing =

  end
  
  def self.today()
    time = Time.new;
    return time.utc.strftime("%Y%m%d%H");
  end
  
  
end

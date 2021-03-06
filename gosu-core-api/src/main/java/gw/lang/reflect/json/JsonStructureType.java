package gw.lang.reflect.json;

import gw.lang.reflect.ActualName;

import java.util.HashMap;
import java.util.Map;

/**
 */
class JsonStructureType implements IJsonParentType
{
  private IJsonParentType _parent;
  private String _name;
  private Map<String, IJsonType> _members;
  private Map<String, IJsonParentType> _innerTypes;

  JsonStructureType( IJsonParentType parent, String name )
  {
    _parent = parent;
    _name = name;
    _members = new HashMap<>();
    _innerTypes = new HashMap<>();
  }

  public String getName()
  {
    return _name;
  }

  public IJsonParentType getParent()
  {
    return _parent;
  }

  public void addChild( String name, IJsonParentType type )
  {
    _innerTypes.put( name, type );
  }

  public IJsonParentType findChild( String name )
  {
    return _innerTypes.get( name );
  }

  public void addMember( String name, IJsonType type )
  {
    IJsonType existingType = _members.get( name );
    if( existingType != null && existingType != type )
    {
      throw new RuntimeException( "Types disagree for '" + name + "' from array data: " + type.getName() + " vs: " + existingType.getName() );
    }
    _members.put( name, type );
  }

  public IJsonType findMemberType( String name )
  {
    return _members.get( name );
  }

  public void render( StringBuilder sb, int indent, boolean mutable )
  {
    indent( sb, indent );

    String name = getName();
    String identifier = addActualNameAnnotation( sb, indent, name );

    sb.append( "structure " ).append( identifier ).append( " {\n" );
    renderTopLevelFactoryMethods( sb, indent + 2 );
    for( String key : _members.keySet() )
    {
      identifier = addActualNameAnnotation( sb, indent + 2, key );
      indent( sb, indent + 2 );
      sb.append( "property get " ).append( identifier ).append( "(): " ).append( _members.get( key ).getName() ).append( "\n" );
      if( mutable )
      {
        indent( sb, indent + 2 );
        sb.append( "property set " ).append( identifier ).append( "( " ).append( "$value: " ).append( _members.get( key ).getName() ).append( " )\n" );
      }
    }
    for( IJsonParentType child : _innerTypes.values() )
    {
      child.render( sb, indent + 2, mutable );
    }
    indent( sb, indent );
    sb.append( "}\n" );
  }

  private String addActualNameAnnotation( StringBuilder sb, int indent, String name )
  {
    String identifier = makeIdentifier( name );
    if( !identifier.equals( name ) )
    {
      indent( sb, indent );
      sb.append( "@" ).append( ActualName.class.getName() ).append( "( \"" ).append( name ).append( "\" )\n" );
    }
    return identifier;
  }

  private String makeIdentifier( String name )
  {
    String identifier = ReservedWordMapping.getIdentifierForName( name );
    if( !identifier.equals( name ) )
    {
      return identifier;
    }

    StringBuilder sb = new StringBuilder();
    for( int i = 0; i < name.length(); i++ )
    {
      char c = name.charAt( i );
      if( c == '_' || c =='$' || (i == 0 ? Character.isLetter( c ) : Character.isLetterOrDigit( c )) )
      {
        sb.append( c );
      }
      else
      {
        sb.append( '_' );
      }
    }
    return sb.toString();
  }

  private void renderTopLevelFactoryMethods( StringBuilder sb, int indent )
  {
    if( _parent != null )
    {
      // Only add factory methods to top-level json structure
      return;
    }

    indent( sb, indent );
    sb.append( "static function fromJson( jsonText: String ): " ).append( getName() ).append( " {\n" );
    indent( sb, indent );
    sb.append( "  return gw.lang.reflect.json.Json.fromJson( jsonText ) as " ).append( getName() ).append( "\n" );
    indent( sb, indent );
    sb.append( "}\n" );
    indent( sb, indent );
    sb.append( "static function fromJsonUrl( url: String ): " ).append( getName() ).append( " {\n" );
    indent( sb, indent );
    sb.append( "  return new java.net.URL( url ).JsonContent\n" );
    indent( sb, indent );
    sb.append( "}\n" );
    indent( sb, indent );
    sb.append( "static function fromJsonUrl( url: java.net.URL ): " ).append( getName() ).append( " {\n" );
    indent( sb, indent );
    sb.append( "  return url.JsonContent\n" );
    indent( sb, indent );
    sb.append( "}\n" );
    indent( sb, indent );
    sb.append( "static function fromJsonFile( file: java.io.File ) : " ).append( getName() ).append( " {\n" );
    indent( sb, indent );
    sb.append( "  return fromJsonUrl( file.toURI().toURL() )\n" );
    indent( sb, indent );
    sb.append( "}\n" );
  }

  private void indent( StringBuilder sb, int indent )
  {
    for( int i = 0; i < indent; i++ )
    {
      sb.append( ' ' );
    }
  }
}

package org.xsj.android.spr1ng.core;

public class Bean {
	public enum ScopeType{
		singleton,
		prototype
	}
	public String name;
	public Class<?> clazz;
	public ScopeType scope;
	public Object object;
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Bean other = (Bean) obj;
		if (clazz == null) {
			if (other.clazz != null)
				return false;
		} else if (clazz != other.clazz)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (object == null) {
			if (other.object != null)
				return false;
		} else if (!object.equals(other.object))
			return false;
		if (scope != other.scope)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Bean [name=" + name + ", clazz=" + clazz + ", scope=" + scope
				+ ", object=" + object + "]";
	}

	
	
	
}

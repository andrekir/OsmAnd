package net.osmand.data;

import net.osmand.Location;
import net.osmand.osm.AbstractPoiType;
import net.osmand.osm.MapPoiTypes;
import net.osmand.osm.PoiCategory;
import net.osmand.osm.PoiType;
import net.osmand.util.Algorithms;
import net.osmand.GPXUtilities;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import gnu.trove.list.array.TIntArrayList;


public class Amenity extends MapObject {

	public static final String WEBSITE = "website";
	public static final String PHONE = "phone";
	public static final String MOBILE = "mobile";
	public static final String DESCRIPTION = "description";
	public static final String ROUTE = "route";
	public static final String OPENING_HOURS = "opening_hours";
	public static final String SERVICE_TIMES = "service_times";
	public static final String COLLECTION_TIMES = "collection_times";
	public static final String CONTENT = "content";
	public static final String CUISINE = "cuisine";
	public static final String WIKIDATA = "wikidata";
	public static final String WIKIMEDIA_COMMONS = "wikimedia_commons";
	public static final String MAPILLARY = "mapillary";
	public static final String DISH = "dish";
	public static final String REF = "ref";
	public static final String OSM_DELETE_VALUE = "delete";
	public static final String OSM_DELETE_TAG = "osmand_change";
	public static final String IMAGE_TITLE = "image_title";
	public static final String IS_PART = "is_part";
	public static final String IS_PARENT_OF = "is_parent_of";
	public static final String IS_AGGR_PART = "is_aggr_part";
	public static final String CONTENT_JSON = "content_json";
	public static final String ROUTE_ID = "route_id";
	public static final String ROUTE_SOURCE = "route_source";
	public static final String ROUTE_NAME = "route_name";
	public static final String COLOR = "color";
	public static final String LANG_YES = "lang_yes";
	public static final String GPX_ICON = "gpx_icon";
	public static final String TYPE = "type";
	public static final String SUBTYPE = "subtype";
	private static final String NAME = "name";
	public static final String SEPARATOR = ";;";

	private String subType;
	private PoiCategory type;
	// duplicate for fast access
	private String openingHours;
	private Map<String, String> additionalInfo;
	private AmenityRoutePoint routePoint; // for search on path
	// context menu geometry;
	private TIntArrayList y;
	private TIntArrayList x;

	public Amenity() {
	}

	public static class AmenityRoutePoint {
		public double deviateDistance;
		public boolean deviationDirectionRight;
		public Location pointA;
		public Location pointB;
	}

	public PoiCategory getType() {
		return type;
	}

	public String getSubType() {
		return subType;
	}

	public void setType(PoiCategory type) {
		this.type = type;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}

	public String getOpeningHours() {
		return openingHours;
	}

	public String getAdditionalInfo(String key) {
		if (additionalInfo == null) {
			return null;
		}
		String str = additionalInfo.get(key);
		str = unzipContent(str);
		return str;
	}


	// this method should be used carefully
	public Map<String, String> getInternalAdditionalInfoMap() {
		if (additionalInfo == null) {
			return Collections.emptyMap();
		}
		return additionalInfo;
	}
	
	public Collection<String> getAdditionalInfoValues(boolean excludeZipped) {
		if (additionalInfo == null) {
			return Collections.emptyList();
		}
		boolean zipped = false;
		for(String v : additionalInfo.values()) {
			if(isContentZipped(v)) {
				zipped = true;
				break;
			}
		}
		if(zipped) {
			List<String> r = new ArrayList<>(additionalInfo.size());
			for(String str : additionalInfo.values()) {
				if(excludeZipped && isContentZipped(str)) {
					
				} else {
					r.add(unzipContent(str));
				}
			}
			return r;
		} else {
			return additionalInfo.values();
		}
	}
	
	public Collection<String> getAdditionalInfoKeys() {
		if (additionalInfo == null) {
			return Collections.emptyList();
		}
		return additionalInfo.keySet();
	}

	public void setAdditionalInfo(Map<String, String> additionalInfo) {
		this.additionalInfo = null;
		openingHours = null;
		if (additionalInfo != null) {
			Iterator<Entry<String, String>> it = additionalInfo.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, String> e = it.next();
				setAdditionalInfo(e.getKey(), e.getValue());
			}
		}
	}

	public void setRoutePoint(AmenityRoutePoint routePoint) {
		this.routePoint = routePoint;
	}

	public AmenityRoutePoint getRoutePoint() {
		return routePoint;
	}

	public void setAdditionalInfo(String tag, String value) {
		if ("name".equals(tag)) {
			setName(value);
		} else if (tag.startsWith("name:")) {
			setName(tag.substring("name:".length()), value);
		} else {
			if (this.additionalInfo == null) {
				this.additionalInfo = new LinkedHashMap<String, String>();
			}
			this.additionalInfo.put(tag, value);
			if (OPENING_HOURS.equals(tag)) {
				this.openingHours = unzipContent(value);
			}
		}
	}

	@Override
	public String toStringEn() {
		return super.toStringEn() + ": " + type.getKeyName() + ":" + subType;
	}

	@Override
	public String toString() {
		return type.getKeyName() + ": " + subType + " " + getName();
	}

	public String getSite() {
		return getAdditionalInfo(WEBSITE);
	}

	public void setSite(String site) {
		setAdditionalInfo(WEBSITE, site);
	}

	public String getPhone() {
		return getAdditionalInfo(PHONE);
	}

	public void setPhone(String phone) {
		setAdditionalInfo(PHONE, phone);
	}

	public String getColor() {
		return getAdditionalInfo(COLOR);
	}

	public String getGpxIcon() {
		return getAdditionalInfo(GPX_ICON);
	}


	public String getContentLanguage(String tag, String lang, String defLang) {
		if (lang != null) {
			String translateName = getAdditionalInfo(tag + ":" + lang);
			if (!Algorithms.isEmpty(translateName)) {
				return lang;
			}
		}
		String plainContent = getAdditionalInfo(tag);
		if (!Algorithms.isEmpty(plainContent)) {
			return defLang;
		}
		String enName = getAdditionalInfo(tag + ":en");
		if (!Algorithms.isEmpty(enName)) {
			return "en";
		}
		int maxLen = 0;
		String lng = defLang;
		for (String nm : getAdditionalInfoKeys()) {
			if (nm.startsWith(tag + ":")) {
				String key = nm.substring(tag.length() + 1);
				String cnt = getAdditionalInfo(tag + ":" + key);
				if (!Algorithms.isEmpty(cnt) && cnt.length() > maxLen) {
					maxLen = cnt.length();
					lng = key;
				}
			}
		}
		return lng;
	}

	public Set<String> getSupportedContentLocales() {
		Set<String> supported = new TreeSet<>();
		supported.addAll(getNames("content", "en"));
		supported.addAll(getNames("description", "en"));
		return supported;
	}

	public List<String> getNames(String tag, String defTag) {
		List<String> l = new ArrayList<String>();
		for (String nm : getAdditionalInfoKeys()) {
			if (nm.startsWith(tag + ":")) {
				l.add(nm.substring(tag.length() + 1));
			} else if (nm.equals(tag)) {
				l.add(defTag);
			}
		}
		return l;
	}

	public String getTagSuffix(String tagPrefix) {
		for (String infoTag : getAdditionalInfoKeys()) {
			if (infoTag.startsWith(tagPrefix)) {
				if (infoTag.length() > tagPrefix.length()) {
					return infoTag.substring(tagPrefix.length());
				}
			}
		}
		return null;
	}

	public String getTagContent(String tag) {
		return getTagContent(tag, null);
	}

	public String getTagContent(String tag, String lang) {
		String translateName = getStrictTagContent(tag, lang);
		if (translateName != null) {
			return translateName;
		}
		for (String nm : getAdditionalInfoKeys()) {
			if (nm.startsWith(tag + ":")) {
				return getAdditionalInfo(nm);
			}
		}
		return null;
	}

	public String getRef(){
		return getAdditionalInfo(REF);
	}

	public String getRouteId(){
		return getAdditionalInfo(ROUTE_ID);
	}

	public String getStrictTagContent(String tag, String lang) {
		if (lang != null) {
			String translateName = getAdditionalInfo(tag + ":" + lang);
			if (!Algorithms.isEmpty(translateName)) {
				return translateName;
			}
		}
		String plainName = getAdditionalInfo(tag);
		if (!Algorithms.isEmpty(plainName)) {
			return plainName;
		}
		String enName = getAdditionalInfo(tag + ":en");
		if (!Algorithms.isEmpty(enName)) {
			return enName;
		}
		return null;
	}

	public String getDescription(String lang) {
		String info = getTagContent(DESCRIPTION, lang);
		if (!Algorithms.isEmpty(info)) {
			return info;
		}
		return getTagContent(CONTENT, lang);
	}

	public void setDescription(String description) {
		setAdditionalInfo(DESCRIPTION, description);
	}

	public void setOpeningHours(String openingHours) {
		setAdditionalInfo(OPENING_HOURS, openingHours);
	}

	public boolean comparePoi(Amenity thatObj) {
		return this.compareObject(thatObj) &&
				Algorithms.objectEquals(this.type.getKeyName(), thatObj.type.getKeyName()) &&
				Algorithms.objectEquals(this.subType, thatObj.subType) &&
				Algorithms.objectEquals(this.additionalInfo, thatObj.additionalInfo);
	}
	
	@Override
	public int compareTo(MapObject o) {
		int cmp = super.compareTo(o);
		if(cmp == 0 && o instanceof Amenity) {
			int kn = ((Amenity) o).getType().getKeyName().compareTo(getType().getKeyName());
			if(kn == 0) {
				kn = ((Amenity) o).getSubType().compareTo(getSubType());
			}
			return kn;
		}
		return cmp;
	}

	@Override
	public boolean equals(Object o) {
		boolean res = super.equals(o);
		if (res && o instanceof Amenity) {
			return Algorithms.stringsEqual(((Amenity) o).getType().getKeyName(), getType().getKeyName())
					&& Algorithms.stringsEqual(((Amenity) o).getSubType(), getSubType());
		}
		return res;
	}

	public TIntArrayList getY() {
		if(y == null) {
			y = new TIntArrayList();
		}
		return y;
	}
	
	public TIntArrayList getX() {
		if (x == null) {
			x = new TIntArrayList();
		}
		return x;
	}

	public boolean isClosed() {
		return OSM_DELETE_VALUE.equals(getAdditionalInfo(OSM_DELETE_TAG));
	}

	public JSONObject toJSON() {
		JSONObject json = super.toJSON();
		json.put("subType", subType);
		json.put("type", type.getKeyName());
		json.put("openingHours", openingHours);
		if (additionalInfo != null && additionalInfo.size() > 0) {
			JSONObject additionalInfoObj = new JSONObject();
			for (Entry<String, String> e : additionalInfo.entrySet()) {
				additionalInfoObj.put(e.getKey(), e.getValue());
			}
			json.put("additionalInfo", additionalInfoObj);
		}

		return json;
	}

	public static Amenity parseJSON(JSONObject json) {
		Amenity a = new Amenity();
		MapObject.parseJSON(json, a);

		if (json.has("subType")) {
			a.subType = json.getString("subType");
		}
		if (json.has("type")) {
			String categoryName = json.getString("type");
			a.setType(MapPoiTypes.getDefault().getPoiCategoryByName(categoryName));
		} else {
			a.setType(MapPoiTypes.getDefault().getOtherPoiCategory());
		}
		if (json.has("openingHours")) {
			a.openingHours = json.getString("openingHours");
		}
		if (json.has("additionalInfo")) {
			JSONObject namesObj = json.getJSONObject("additionalInfo");
			a.additionalInfo = new HashMap<>();
			Iterator<String> iterator = namesObj.keys();
			while (iterator.hasNext()) {
				String key = iterator.next();
				String value = namesObj.getString(key);
				a.additionalInfo.put(key, value);
			}
		}
		return a;
	}

	public Map<String, String> toTagValue(String privatePrefix, String osmPrefix, String collapsablePrefix, MapPoiTypes poiTypes) {
		Map<String, String> result = new HashMap<String, String>();
		Map<String, List<PoiType>> collectedPoiAdditionalCategories = new HashMap<>();

		if (getName() != null) {
			result.put(privatePrefix + NAME, getName());
		}
		if (subType != null) {
			result.put(privatePrefix + SUBTYPE, subType);
		}
		if (type != null) {
			result.put(privatePrefix + TYPE, type.getKeyName());
		}
		if (openingHours != null) {
			result.put(privatePrefix + OPENING_HOURS, openingHours);
		}
		if (additionalInfo != null && additionalInfo.size() > 0) {
			for (Entry<String, String> e : additionalInfo.entrySet()) {
				String key = e.getKey();
				String vl = e.getValue();

				//collect tags with categories and skip
				AbstractPoiType pt = poiTypes.getAnyPoiAdditionalTypeByKey(key);
				if (pt == null && !Algorithms.isEmpty(vl) && vl.length() < 50) {
					pt = poiTypes.getAnyPoiAdditionalTypeByKey(key + "_" + vl);
				}
				PoiType pType = null;
				if (pt != null) {
					pType = (PoiType) pt;
					if (pType.isFilterOnly()) {
						continue;
					}
				}
				if (pType != null && !pType.isText()) {
					String categoryName = pType.getPoiAdditionalCategory();
					if (!Algorithms.isEmpty(categoryName)) {
						List<PoiType> poiAdditionalCategoryTypes = collectedPoiAdditionalCategories.get(categoryName);
						if (poiAdditionalCategoryTypes == null) {
							poiAdditionalCategoryTypes = new ArrayList<>();
							collectedPoiAdditionalCategories.put(categoryName, poiAdditionalCategoryTypes);
						}
						poiAdditionalCategoryTypes.add(pType);
						continue;
					}
				}

				//save all other values to separate lines
				if (key.endsWith(OPENING_HOURS)) {
					continue;
				}
				if (!GPXUtilities.HIDING_EXTENSIONS_AMENITY_TAGS.contains(key)) {
					key = osmPrefix + key;
				}
				result.put(key, e.getValue());
			}

			//join collected tags by category into one string
			for (Map.Entry<String, List<PoiType>> e : collectedPoiAdditionalCategories.entrySet()) {
				String categoryName = collapsablePrefix + e.getKey();
				List<PoiType> categoryTypes = e.getValue();
				if (categoryTypes.size() > 0) {
					StringBuilder sb = new StringBuilder();
					for (PoiType pt : categoryTypes) {
						if (sb.length() > 0) {
							sb.append(SEPARATOR);
						}
						sb.append(pt.getKeyName());
					}
					result.put(categoryName, sb.toString());
				}
			}
		}
		return result;
	}


	//TODO: delete!
	public static Amenity fromTagValue(Map<String, String> map, String privatePrefix, String osmPrefix){
		if (!Algorithms.isEmpty(map)) {
			PoiCategory type = null;
			String subtype = null;
			String openingHours = null;
			HashMap additionalInfo = new HashMap<String, String>();

			for (Entry<String, String> entry : map.entrySet()) {
				if (entry.getKey().startsWith(privatePrefix)) {
					String shortKey = entry.getKey().replace(privatePrefix, "");
					if (shortKey.equals(TYPE)) {
						type = MapPoiTypes.getDefault().getPoiCategoryByName(entry.getValue());
						if (type == null) {
							type = MapPoiTypes.getDefault().getOtherPoiCategory();
						}
					} else if (shortKey.equals(SUBTYPE)) {
						subtype = entry.getValue();
					} else if (shortKey.equals(OPENING_HOURS)) {
						openingHours = entry.getValue();
					}
				} else if (entry.getKey().startsWith(osmPrefix)) {
					String shortKey = entry.getKey().replace(osmPrefix, "");
					additionalInfo.put(shortKey, entry.getValue());
				} else {
					List<String> reservedOsmandTags = GPXUtilities.EXTENSIONS_WITH_OSMAND_PREFIX;
					if (!reservedOsmandTags.contains(entry.getKey())) {
						additionalInfo.put(entry.getKey(), entry.getValue());
					}
				}
			}
			if (additionalInfo.size() > 0 || openingHours != null) {
				Amenity amenity = new Amenity();
				if (type != null) {
					amenity.setType(type);
				} else {
					amenity.setType(type = MapPoiTypes.getDefault().getOtherPoiCategory());
				}
				if (subtype != null) {
					amenity.subType = subtype;
				}
				if (openingHours != null) {
					amenity.openingHours = openingHours;
					additionalInfo.put(OPENING_HOURS, openingHours);
				}
				amenity.additionalInfo = additionalInfo;
				return amenity;
			}
		}
		return null;
	}

	//TODO: delete!
	public void updateWithAmenity(Amenity newAmenity) {
		if (newAmenity.type != null) {
			this.type = newAmenity.type;
		}
		if (newAmenity.subType != null) {
			this.subType = newAmenity.subType;
		}
		if (newAmenity.openingHours != null) {
			this.openingHours = newAmenity.openingHours;
		}

		this.additionalInfo.putAll(newAmenity.additionalInfo);
	}
}
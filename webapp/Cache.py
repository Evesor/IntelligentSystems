from google.appengine.api import memcache

DataName = "Data"


def get_cache_data():
	return memcache.get(DataName)


def cache_data(data_in):
	data_cache = memcache.get(DataName)
	if data_cache:
		data_cache.append(data_in)
	else:
		data_cache = [data_in]
	memcache.set(DataName, data_cache)



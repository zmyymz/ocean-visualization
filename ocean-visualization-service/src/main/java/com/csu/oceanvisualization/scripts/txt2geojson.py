import pandas as pd
import geopandas as gpd
from shapely.geometry import LineString
import io
import sys

def txt2geojson(input_path, output_path):
    col = ['date', 'time', 'longitude', 'latitude', 'min pressure', 'max wind speed(intensity)']
    df = pd.read_csv(input_path, names=col, sep=' ')
    gdf = gpd.GeoDataFrame(df, geometry=gpd.points_from_xy(df.longitude, df.latitude), crs='EPSG:4326')
    return gdf.to_json()
#   gdf.to_file(output_path, driver='GeoJSON')

# def txt2geojson(input_path, output_path):
#   print(input_path)
#   print(output_path)

if __name__ == "__main__":
#    print('程序名称为：{}，第一个参数为：{}，第二个参数为：{}'.format(sys.argv[0], sys.argv[1], sys.argv[2]))
    print(txt2geojson(sys.argv[1], sys.argv[2]))
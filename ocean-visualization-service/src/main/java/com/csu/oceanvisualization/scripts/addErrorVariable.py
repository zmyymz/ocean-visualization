import netCDF4 as nc
import numpy as np
import sys
# !pip uninstall netCDF4
# !pip install netCDF4==1.3.1

def getVariableArray(ncdata, var):
  var_data = ncdata[var][:]
  return var_data

def addErrorVariable(inputPath):
  # ['SSH_202109']
  filename = inputPath
  f = nc.Dataset(filename,'r+',format='NETCDF4')   #读取.nc文件，传入f中。此时f包含了该.nc文件的全部信息
  all_vars = f.variables.keys()   #获取所有变量名称

  variablesList = []
  for v in all_vars:
    if("_" in v):
      variablesList.append(v)

  temp = variablesList[0]
  prefix = temp[0:temp.rfind('_', 1)+1] # SWH_
  # prefix

  for v in variablesList:
    # var_data = f[var][:]   #获取变量的数据
    # var_data = np.array(var_data)  #转化为np.array数组
    if(("pred".lower() in v) or ("fore".lower() in v)):
      pred_array = getVariableArray(f,v)
    elif("Corr" in v or "Corr".lower() in v):
      Corr_array = getVariableArray(f,v)
    elif("Real" in v or "Real".lower() in v):
      Real_array = getVariableArray(f,v)

  Error_Before = pred_array - Real_array
  Error_After = Corr_array - Real_array


  if("temp" in prefix):
    #新创建一个多维度变量，并写入数据，‘变量名称’，‘数据类型’，‘基础维度信息’
    Error_Before_var = prefix + 'Error_Before'
    f.createVariable(Error_Before_var, np.float64, ('time', 'lat', 'lon'))
    f.variables[Error_Before_var][:] = Error_Before

    #新创建一个多维度变量，并写入数据，‘变量名称’，‘数据类型’，‘基础维度信息’
    Error_After_var = prefix + 'Error_After'
    f.createVariable(Error_After_var, np.float64, ('time', 'lat', 'lon'))
    f.variables[Error_After_var][:] = Error_After
  else:
    #新创建一个多维度变量，并写入数据，‘变量名称’，‘数据类型’，‘基础维度信息’
    Error_Before_var = prefix + 'Error_Before'
    f.createVariable(Error_Before_var, np.float64, ('t', 'lat', 'lon'))
    f.variables[Error_Before_var][:] = Error_Before

    #新创建一个多维度变量，并写入数据，‘变量名称’，‘数据类型’，‘基础维度信息’
    Error_After_var = prefix + 'Error_After'
    f.createVariable(Error_After_var, np.float64, ('t', 'lat', 'lon'))
    f.variables[Error_After_var][:] = Error_After

  # print(f.variables.keys())


if __name__ == "__main__":
#     print('程序名称为：{}，第一个参数为：{}，第二个参数为：{}'.format(sys.argv[0], sys.argv[1], sys.argv[2]))
    addErrorVariable(sys.argv[1])